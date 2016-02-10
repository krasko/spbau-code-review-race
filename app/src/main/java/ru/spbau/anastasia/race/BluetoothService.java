package ru.spbau.anastasia.race;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BluetoothService extends Service {

    public boolean isServer;
    public boolean isBegin;
    public static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    public static final int NOTIFICATION_ID = 1;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private AcceptThread acceptThread;
    private CreateConnectionThread createConnectionThread;
    private AcceptConnectionThread acceptConnectionThread;

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
            } catch (IOException ignored) {
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
        } catch (IOException ignored) {
            Log.d(TAG, "Socket was closed with errror.");
        }
    }

    private void closeServerSocket(BluetoothServerSocket bluetoothServerSocket) {
        try {
            bluetoothServerSocket.close();
        } catch (IOException ignored) {
            Log.d(TAG, "ServerSocket was closed with errror.");
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

    private MessageReceiver MessageReceiver;
    List<Integer> sizes = new ArrayList<>();
    List<byte[]> cache = new ArrayList<>();

    public void setMessageReceiver(MessageReceiver MessageReceiver) {
        this.MessageReceiver = MessageReceiver;

        for (int i = 0; i < cache.size(); ++i) {
            MessageReceiver.process(sizes.get(i), cache.get(i));
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

    public void showNotification(Class<?> aClass, String string) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(string)
                .setContentText(string)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, aClass), 0));

        startForeground(NOTIFICATION_ID, builder.build());
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
            Integer integer = (Integer) futureTask.get();
        } catch (InterruptedException e) {

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
            } catch (IOException ignored) {
                Log.d(TAG, "ServerSocket was created with errror.");
            }
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                    isServer = true;
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    try {
                        connect(socket);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    closeServerSocket(serverSocket);
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
            } catch (IOException ignored) {
                Log.d(TAG, "Socket was created with errror.");
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
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
            } catch (IOException ignored) {
                Log.d(TAG, "AcceptConnectionThread was created with error");
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
                        MessageReceiver.process(bytes, buffer);
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
                Log.d(TAG, "Trying to write: " + bytes.toString());
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
