package ru.spbau.anastasia.race;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

public class RoadForTwo extends BaseActivity implements mGame.SceneListener {

    public static final byte GAME_OVER = -1;
    public static final byte FINISH_ACTIVITY = -2;

    private static final String TAG = "RoadForTwo";

    private static final byte GAME_READY = -3;

    private SensorManager sensorManager;
    private Sensor sensor;
    private TwoPlayerGameView gameView;
    private mGameForTwo game;

    private BluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isServer = getIntent().getExtras().getBoolean("isServer");

        setContentView(R.layout.activity_road_for_two);
        gameView = (TwoPlayerGameView) findViewById(R.id.game_view);
        gameView.initBackground(numOfTheme);


        Sound sound = new Sound(getAssets(), numOfTheme, 0);
        sound.isStopped = !isSound;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        game = new mGameForTwo(getResources(), numOfTheme, sound);
        synchronized (game) {
            game.isServer = isServer;
            gameView.game = game;
            game.sceneListener = this;

            bindService(new Intent(this, BluetoothService.class), connection, BIND_AUTO_CREATE);
        }
    }

    public void onBackButtonClickRoadForOne(View view) {
        finish();
    }

    private boolean opponentStarted = false;
    private boolean started = false;

    BluetoothService.MessageReceiver inGameReceiver = new BluetoothService.MessageReceiver() {
        @Override
        public void process(int bytes, byte[] buffer) {
            synchronized (game) {
                if (buffer[0] == GAME_OVER) {
                    game.stop();
                    return;
                }
                if (buffer[0] == FINISH_ACTIVITY) {
                    finish();
                    return;
                }
                FileForSent comeIn = new FileForSent(buffer, bytes);
                game.registerMsg(comeIn);
                Log.d(TAG, "MessageReceived : " + Arrays.toString(comeIn.toMsg()));
            }
        }
    };

    BluetoothService.MessageReceiver initialReceiver = new BluetoothService.MessageReceiver() {
        @Override
        public void process(int bytes, byte[] buffer) {
            opponentStarted = true;
            if (started) {
                game.start();
            }
            btService.setMessageReceiver(inGameReceiver);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (game) {
                btService = ((BluetoothService.BtBinder) service).getService();

                btService.setMessageReceiver(initialReceiver);
                Log.d("SERVER", Boolean.toString(game.isServer));
                started = true;
                btService.write(new byte[]{GAME_READY});
                if (opponentStarted) {
                    game.start();
                    gameView.playerInfo.show();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(game, sensor, SensorManager.SENSOR_DELAY_GAME);
        game.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(game);
        game.pause();
    }

    @Override
    public void onGameOver() {
        gameView.gameStopped = true;
        btService.write(new byte[]{GAME_OVER});
    }

    @Override
    protected void onDestroy() {
        game.stop();
        super.onDestroy();
    }

    @Override
    public void onNextStep() {
        FileForSent toSend = game.getMsgToSend();
        if (toSend != null) {
            btService.write(toSend.toMsg());
            Log.d(TAG, "MessageSent : " + Arrays.toString(toSend.toMsg()));
        }
    }

    public void onClickButtonBackRoadForTwo(View view) {
        finish();
    }
}
