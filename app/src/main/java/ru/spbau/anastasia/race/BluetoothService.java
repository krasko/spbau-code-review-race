package ru.spbau.anastasia.race;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    private boolean isConnectionBegin;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    private AcceptThread acceptThread;
    private CreateConnectionThread createConnectionThread;
    private AcceptConnectionThread acceptConnectionThread;

    private MessageReceiver messageReceiver;

    private OnConnected onConnected;

    private final IBinder binder = new BtBinder();

    public class BtBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public interface MessageReceiver {
        void process(int bytes, byte[] buffer);
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "SocketData: " + (bluetoothSocket == null ? "" : bluetoothSocket.toString()) + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: serverSocket was created with an error");
                makeToast("ServerSocket was created with an error");
            }
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                    if (socket != null) {
                        try {
                            connect(socket);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        closeServerSocket(serverSocket);
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void cancel() {
            closeServerSocket(serverSocket);
        }
    }

    private class CreateConnectionThread extends Thread {
        private BluetoothSocket socket;
        public static final String TAG = "CreateConnectionThread";

        public CreateConnectionThread(String address) {
            try {
                socket = bluetoothAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + bluetoothSocket.toString() + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: socket was created with an error");
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                Log.d(TAG, Boolean.toString(socket.isConnected()));
            } catch (IOException connectException) {
                closeSocket(socket);
                return;
            }

            BluetoothSocket tmp = socket;
            socket = null;
            try {
                connect(tmp);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            if (socket != null) {
                closeSocket(socket);
            }
        }
    }

    private class AcceptConnectionThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public AcceptConnectionThread() {
            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;

            try {
                tmpInputStream = bluetoothSocket.getInputStream();
                tmpOutputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + bluetoothSocket.toString() + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: tmpStreams were created with an error");
            }

            inputStream = tmpInputStream;
            outputStream = tmpOutputStream;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    Log.d(TAG, "read");
                    try {
                        messageReceiver.process(bytes, buffer);
                    } catch (NullPointerException ignored) {
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.d(TAG, "Trying to write: " + Arrays.toString(bytes));
            } catch (IOException ignored) {
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public boolean isConnectionBegin() {
        return isConnectionBegin;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (acceptThread != null) {
            acceptThread.cancel();
        }

        if (createConnectionThread != null) {
            createConnectionThread.cancel();
        }

        if (acceptConnectionThread != null) {
            acceptConnectionThread.cancel();
        }

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException error) {
                Log.d(TAG, "SocketData: " + bluetoothSocket.toString() + "; exception data: "
                        + error.toString()
                        + "; BluetoothService.java: bluetoothSocket was closed with an error");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static class BtUnavailableException extends Exception {
        public BtUnavailableException() {
            super("Bluetooth is not supported.");
        }
    }

    public void initBtAdapter() throws BtUnavailableException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new BtUnavailableException();
        }
        isConnectionBegin = false;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public interface OnConnected {
        FutureTask success();
    }

    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public synchronized void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public synchronized void startConnectThread(String address) {
        createConnectionThread = new CreateConnectionThread(address);
        createConnectionThread.start();
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public void write(byte[] bytes) {
        acceptConnectionThread.write(bytes);
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void closeConnection() {
        if (acceptConnectionThread != null) {
            acceptConnectionThread.cancel();
            acceptConnectionThread = null;
        }
    }

    private synchronized void connect(BluetoothSocket socket) throws ExecutionException, InterruptedException {
        this.bluetoothSocket = socket;

        if (acceptThread != null) {
            acceptThread.cancel();
        }

        if (createConnectionThread != null) {
            createConnectionThread.cancel();
        }

        FutureTask futureTask = onConnected.success();
        try {
            futureTask.get();
        } catch (InterruptedException ignored) {

        }

        isConnectionBegin = true;
        acceptConnectionThread = new AcceptConnectionThread();
        acceptConnectionThread.start();
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void closeSocket(BluetoothSocket bluetoothSocket) {
        try {
            bluetoothSocket.close();
        } catch (IOException error) {
            Log.d(TAG, "SocketData: " + this.bluetoothSocket.toString() + "; exception data: "
                    + error.toString()
                    + "; BluetoothService.java: bluetoothSocket was closed with an error");
        }
    }

    private void closeServerSocket(BluetoothServerSocket bluetoothServerSocket) {
        try {
            bluetoothServerSocket.close();
        } catch (IOException error) {
            Log.d(TAG, "SocketData: " + bluetoothSocket.toString() + "; exception data: "
                    + error.toString()
                    + "; BluetoothService.java: bluetoothServerSocket was closed with an error");
        }
    }
}
