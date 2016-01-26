package ru.spbau.anastasia.race;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class RoadForOne extends Activity implements mScene.SceneListener {

    private SceneManager sceneManager;
    private SensorManager sensorManager;
    private Sensor sensor;
    private ImageButton pause, restart;
    private View.OnClickListener onPauseListener, onResumeListener;
    private OnePlayerGameView gameView;

    private Runnable activateRestartButton = new Runnable() {
        @Override
        public void run() {
            restart.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int numOfTheme = getIntent().getExtras().getInt("theme");

        int player_id = getIntent().getExtras().getInt("player");
        setContentView(R.layout.activity_road_for_one);

        gameView = (OnePlayerGameView) findViewById(R.id.game_view);
        boolean isSound = getIntent().getExtras().getBoolean("sound");

        Sound sound = new Sound(getAssets(), numOfTheme, 0);
        sound.isStopped = !isSound;

        final mScene scene = new mScene(getResources(), mScene.SINGLE_PLAY, numOfTheme, sound);
        synchronized (scene) {

            sceneManager = new SceneManager(scene);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(sceneManager, sensor, SensorManager.SENSOR_DELAY_GAME);

            scene.width = gameView.getWidth();
            scene.height = gameView.getHeight();
            scene.player_id = player_id;
            sceneManager = new SceneManager(scene);

            gameView.scene = scene;
            pause = (ImageButton) findViewById(R.id.pause);
            restart = (ImageButton) findViewById(R.id.restart);
        }

        onPauseListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scene.stop();
                pause.setImageResource(R.drawable.play);
                pause.setOnClickListener(onResumeListener);
            }
        };

        onResumeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scene.start();
                pause.setImageResource(R.drawable.pause);
                pause.setOnClickListener(onPauseListener);
            }
        };

        scene.sceneListener = this;

        restart.setVisibility(View.GONE);
        pause.setOnClickListener(onPauseListener);

        gameView.initFon(numOfTheme);

    }

    public void onBackButtonClickRoadForOne(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sceneManager, sensor, SensorManager.SENSOR_DELAY_GAME);
        sceneManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sceneManager);
        sceneManager.stop();
    }

    @Override
    public void onGameOver() {
        gameView.scene.dead = true;
        runOnUiThread(activateRestartButton);
    }

    public void onRestartButtonClick(View view) {
        sceneManager.scene.restart();
        restart.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
    }
}
