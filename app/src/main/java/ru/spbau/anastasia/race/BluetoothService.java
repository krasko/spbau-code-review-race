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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BluetoothService extends Service {

    private boolean isServer;
    private boolean isBegin;
    public static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private AcceptThread acceptThread;
    private CreateConnectionThread createConnectionThread;
    private AcceptConnectionThread acceptConnectionThread;

    public boolean isServer() {
        return isServer;
    }

    public boolean isBegin() {
        return isBegin;
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
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

        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                        + e.toString() + "; BluetoothService.java: 69");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class BtBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder binder = new BtBinder();

    private void closeSocket(BluetoothSocket bluetoothSocket) {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                    + e.toString()
                    + "; BluetoothService.java: 94: socket was closed with an error");
        }
    }

    private void closeServerSocket(BluetoothServerSocket bluetoothServerSocket) {
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                    + e.toString()
                    + "; BluetoothService.java: 99: socket was closed with an error");
        }
    }

    public static class BtUnavailableException extends Exception {
        public BtUnavailableException() {
            super("bluetooth is not supported");
        }
    }

    public void initBtAdapter() throws BtUnavailableException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            throw new BtUnavailableException();
        }
        isBegin = false;
    }

    public boolean isConnected() {
        return acceptConnectionThread != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    public interface OnConnected {
        FutureTask success();
    }

    private OnConnected onConnected;

    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public synchronized void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void startConnectThread(String address) {
        createConnectionThread = new CreateConnectionThread(address);
        createConnectionThread.start();
    }

    public interface MessageReceiver {
        void process(int bytes, byte[] buffer);
    }

    private MessageReceiver messageReceiver;
    List<Integer> sizes = new ArrayList<>();
    List<byte[]> cache = new ArrayList<>();

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;

        for (int i = 0; i < cache.size(); ++i) {
            messageReceiver.process(sizes.get(i), cache.get(i)); // TODO
        }
        cache.clear();
        sizes.clear();
    }

    public void write(byte[] bytes) {
        acceptConnectionThread.write(bytes);
    }

    public BluetoothSocket getBluetoothSocket() {
        return btSocket;
    }

    private synchronized void connect(BluetoothSocket socket) throws ExecutionException, InterruptedException {
        this.btSocket = socket;

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

        isBegin = true;
        acceptConnectionThread = new AcceptConnectionThread();
        acceptConnectionThread.start();
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: 208: socket was created with an error");
                makeToast("ServerSocket was created with an error");
            }
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                    isServer = true;
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
                socket = btAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: 249: socket was created with an error");
            }
        }

        public void run() {
            btAdapter.cancelDiscovery();

            try {
                socket.connect();
                isServer = false;
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
                tmpInputStream = btSocket.getInputStream();
                tmpOutputStream = btSocket.getOutputStream();
            } catch (IOException e) {
                Log.d(TAG, "SocketData: " + btSocket.toString() + "; exception data: "
                        + e.toString()
                        + "; BluetoothService.java: 295: socket was created with an error");
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
                        sizes.add(bytes);
                        cache.add(buffer);
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
                btSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
