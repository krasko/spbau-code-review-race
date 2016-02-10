package ru.spbau.anastasia.race;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class SceneManager implements SensorEventListener {

    public static final byte[] FIRST_MSG = new byte[20];
    public static final int FPS = 10;
    public static final int SCALE_INCREASE = 50;
    protected final mScene scene;
    public float dx, dy;

    private SceneTask task;
    private Timer timer;

    private class SceneTask extends TimerTask {
        @Override
        public void run() {
            synchronized (scene) {
                if (scene.type == mScene.SINGLE_PLAY) {
                    scene.oneStep(dx, dy);
                }
            }
        }
    }

    public byte[] forTwoPlayer(FileForSent msg) {
        synchronized (scene) {
            byte[] bytes = FIRST_MSG;
            try {
                bytes = scene.oneStepForTwo(dx, dy, msg).toMsg();
            } catch (NullPointerException ignored) {
            }
            return bytes;
        }
    }

    public SceneManager(mScene scene_) {
        scene = scene_;
        task = new SceneTask();
        timer = new Timer();
    }

    public void start() {
        timer.schedule(task, 0, 1000 / FPS);
    }

    public void stop() {
        timer.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dx = (float) (-Math.sin(Math.toRadians(event.values[1])) * SCALE_INCREASE);
        dy = (float) (+Math.sin(Math.toRadians(event.values[2])) * SCALE_INCREASE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
