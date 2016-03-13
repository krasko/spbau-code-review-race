package ru.spbau.anastasia.race;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import java.util.Arrays;

import ru.spbau.anastasia.race.Game.mGameForTwo;
import ru.spbau.anastasia.race.Other.FileForSent;

public class RoadForTwo extends BaseRoad {

    public static final byte GAME_OVER = -1;
    public static final byte FINISH_ACTIVITY = -2;

    private static final String TAG = "RoadForTwo";

    private static final byte GAME_READY = -3;
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
                ((mGameForTwo)game).registerMsg(comeIn);
                Log.d(TAG, "MessageReceived : " + Arrays.toString(comeIn.toMsg()));
            }
        }
    };
    private boolean opponentStarted = false;
    private boolean started = false;
    private BluetoothService btService;
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
                Log.d(TAG, game.isServer ? "SERVER" : "CLIENT");
                started = true;
                btService.write(new byte[]{GAME_READY});
                ((TwoPlayerGameView)gameView).playerInfo.show(2000);
                if (opponentStarted) {
                    game.start();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    public void onGameOver() {
        super.onGameOver();
        ((TwoPlayerGameView)gameView).deathInfo.show(5000);
        btService.write(new byte[]{GAME_OVER});
    }

    @Override
    public void onNextStep() {
        FileForSent toSend = ((mGameForTwo)game).getMsgToSend();
        if (toSend != null) {
            btService.write(toSend.toMsg());
            Log.d(TAG, "MessageSent : " + Arrays.toString(toSend.toMsg()));
        }
    }

    public void onClickButtonBackRoadForTwo(View view) {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isServer = getIntent().getExtras().getBoolean("isServer");

        setContentView(R.layout.activity_road_for_two);
        gameView = (TwoPlayerGameView) findViewById(R.id.game_view);
        gameView.initBackground(numOfTheme);

        game = new mGameForTwo(getResources(), numOfTheme, sound);
        synchronized (game) {
            game.isServer = isServer;
            gameView.game = game;
            game.sceneListener = this;

            bindService(new Intent(this, BluetoothService.class), connection, BIND_AUTO_CREATE);
        }
    }

    protected void onBackButtonClickRoadForOne(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
