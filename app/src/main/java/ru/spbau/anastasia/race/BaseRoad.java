package ru.spbau.anastasia.race;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

import ru.spbau.anastasia.race.Game.mGame;
import ru.spbau.anastasia.race.Other.DataBaseHelper;
import ru.spbau.anastasia.race.Other.Sound;
import ru.spbau.anastasia.race.View.OnePlayerGameView;

public abstract class BaseRoad extends BaseActivity implements mGame.SceneListener {

    protected SensorManager sensorManager;
    protected Sensor sensor;
    protected OnePlayerGameView gameView;
    protected mGame game;
    protected Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sound = new Sound(getAssets(), numOfTheme, 0);
        sound.isStopped = !isSound;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

    }

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
    protected void onDestroy() {
        game.stop();
        super.onDestroy();
    }

    @Override
    public void onGameOver() {
        final long newScore = (long) game.countOfRound;
        final long bestScore = DataBaseHelper.bestScore;

        if (newScore > bestScore) {
            DataBaseHelper.setNewBestScore(newScore);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(BaseRoad.this, "Your score: " +
                        (int) newScore + "; last best score: " + bestScore, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        gameView.gameStopped = true;
    }

    @Override
    public void onNextStep() {
    }
}
