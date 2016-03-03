package ru.spbau.anastasia.race;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;

public class ConnectionGame extends Activity {
    public static final int RECEIVED_BLOCK_LENGTH = 4;
    private final int REQUEST_CONNECT = 1;
    private final int REQUEST_ENABLE_BT = 2;
    private final int HANDLER_MESSAGE_GET = 1;
    private boolean isPlayed;
    private int numOfTheme;
    private boolean isSound;

    private SceneManager sceneManager;
    private SensorManager sensorManager;
    private Sensor sensor;
    private mScene scene;

    private static final String STOP_MESSAGE = "stop";
    private static final String START_MESSAGE = "start";
    private ArrayAdapter<String> arrayAdapter;
    private EditText editText;

    private BluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_game);

        isSound = getIntent().getExtras().getBoolean("sound");

        ImageView backgroundImage = (ImageView) findViewById(R.id.ConnectionView);
        numOfTheme = getIntent().getExtras().getInt("theme");

        if (numOfTheme == GameMenu.IS_CHECKED) {
            backgroundImage.setImageResource(R.drawable.connection_game);
        } else {
            backgroundImage.setImageResource(R.drawable.connection_game2);
        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        ((ListView) findViewById(R.id.messages)).setAdapter(arrayAdapter);
        editText = (EditText) findViewById(R.id.edit_text);
        Button button = (Button) findViewById(R.id.send);
        button.setOnClickListener(onClickListener);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
        FrameLayout f = (FrameLayout) findViewById(R.id.gave_layout);
        f.setVisibility(View.GONE);
    }

    public void onClickButtonReconnection(View view) {
        toDeviceChooser();
        btService.write(STOP_MESSAGE.getBytes());
    }

    private void toDeviceChooser() {
        startActivityForResult(new Intent(ConnectionGame.this, Choose.class),
                REQUEST_CONNECT);
    }

    public void onClickButtonStartPlay(View view) {
        try {
            toPlayForTwo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        btService.write(START_MESSAGE.getBytes());
        reviseAllDevices();
    }

    private void reviseAllDevices() {
        sensorManager.unregisterListener(sceneManager);
        sceneManager.stop();
        sensorManager.registerListener(sceneManager, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void toPlayForTwo() throws InterruptedException {
        isPlayed = true;
        TwoPlayerGameView gameView = (TwoPlayerGameView) findViewById(R.id.game_view);

        Sound sound = new Sound(getAssets(), numOfTheme, 0);
        sound.isStopped = !isSound;
        scene = new mScene(getResources(), mScene.PLAY_TOGETHER, numOfTheme, sound);
        synchronized (scene) {
            scene.width = gameView.getWidth();
            scene.height = gameView.getHeight();
            scene.player_id = mScene.FINN;
            scene.isServer = btService.isServer();
            scene.type = mScene.PLAY_TOGETHER;
            sceneManager = new SceneManager(scene);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

            bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

            FrameLayout f = (FrameLayout) findViewById(R.id.gave_layout);
            f.setVisibility(View.VISIBLE);
            FrameLayout l = (FrameLayout) findViewById(R.id.message_frame);
            l.setVisibility(View.GONE);

            FileForSent file = new FileForSent(scene.playerStatus);
            Thread.sleep(1000);
            btService.write(sceneManager.forTwoPlayer(file));
        }
        synchronized (scene) {
            gameView.initBackground(numOfTheme);
            gameView.scene = scene;
        }
    }

    public void onClickButtonBackTwoPlayerOption(View view) {
        if (btService != null && btService.isBegin()) {
            btService.write(STOP_MESSAGE.getBytes());
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onResume() {
        super.onResume();
        if (isPlayed) {
            sensorManager.registerListener(sceneManager, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (btService != null && btService.isBegin()) {
            btService.write(STOP_MESSAGE.getBytes());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlayed) {
            sensorManager.unregisterListener(sceneManager);
            sceneManager.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btService != null && btService.isBegin()) {
            btService.write(STOP_MESSAGE.getBytes());
        }
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @TargetApi(Build.VERSION_CODES.ECLAIR)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            btService.setMessageReceiver(new BluetoothService.MessageReceiver() {
                @Override
                public void process(int bytes, byte[] buffer) {
                    handler.obtainMessage(HANDLER_MESSAGE_GET, bytes, -1, buffer)
                            .sendToTarget();
                }
            });

            try {
                btService.initBtAdapter();
            } catch (BluetoothService.BtUnavailableException e) {
                Toast.makeText(ConnectionGame.this,
                        R.string.bluetooth_absent, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (!btService.getBluetoothAdapter().isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_ENABLE_BT);
            } else if (!btService.isConnected()) {
                startActivityForResult(new Intent(ConnectionGame.this, Choose.class),
                        REQUEST_CONNECT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CONNECT:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        finish();
                        break;
                }
                break;
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        if (!btService.isConnected()) {
                            startActivityForResult(new Intent(ConnectionGame.this, Choose.class),
                                    REQUEST_CONNECT);
                        }
                        break;

                    default:
                        finish();
                        break;
                }
                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btService.write(editText.getText().toString().getBytes());
            arrayAdapter.add("Me: " + editText.getText().toString());
            editText.getText().clear();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] bytes = (byte[]) msg.obj;
            boolean flag = true;
            for (int i = 0; i < RECEIVED_BLOCK_LENGTH; i++) {
                if (bytes[i] != STOP_MESSAGE.getBytes()[i]) {
                    flag = false;
                }
            }
            if (flag) {
                toDeviceChooser();
                arrayAdapter.clear();
            }

            boolean startFlag = true;
            for (int i = 0; i < 5; i++) { // TODO
                if (bytes[i] !=
                        START_MESSAGE.getBytes()[i]) {
                    startFlag = false;
                }
                /* TODO
                 * Why do we need these empty ifs here?

                if (bytes[i] != PAUSE_MESSAGE.getBytes()[i]) {
                }
                if (bytes[i] != RESUME_MESSAGE.getBytes()[i]) {
                }
                */
            }
            if (startFlag) {
                try {
                    toPlayForTwo();
                    reviseAllDevices();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (isPlayed) {
                synchronized (scene) {
                    scene.playerStatus = bytes;
                    new FileForSent(scene.playerStatus);
                    reviseAllDevices();
                    try {
                        if (btService != null && btService.isBegin()) {
                            Thread.sleep(1000 / (SceneManager.FPS));
                        }
                    } catch (InterruptedException ignore) {
                    }
                    FileForSent comeIn = new FileForSent(bytes);
                    byte[] bytes1 = sceneManager.forTwoPlayer(comeIn);
                    Log.d("tag", Arrays.toString(bytes1));
                    btService.write(bytes1);
                    reviseAllDevices();
                }
            } else {
                arrayAdapter.add(btService.getBluetoothSocket().getRemoteDevice().getName() + ": " +
                        new String((byte[]) msg.obj, 0, msg.arg1));
            }
        }
    };

    public void onRestartButtonClick(View view) {
    }

    public void onClickButtonBackRoadForTwo(View view) {
        if (btService != null && btService.isBegin()) {
            btService.write(STOP_MESSAGE.getBytes());
        }
        finish();
    }

}
