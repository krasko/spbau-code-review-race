package ru.spbau.anastasia.race.Game;

import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ru.spbau.anastasia.race.Sound;
import ru.spbau.anastasia.race.Sprites.mBackgroundSprite;
import ru.spbau.anastasia.race.Sprites.mBarrierSprite;
import ru.spbau.anastasia.race.Sprites.mBasic;
import ru.spbau.anastasia.race.Sprites.mLive;
import ru.spbau.anastasia.race.Sprites.mPlayerSprite;
import ru.spbau.anastasia.race.mLayer;

public abstract class mGame implements SensorEventListener {

    public static int NUM_BARRIERS_IN_LAYERS = 0;
    public static final double DELTA_COUNT = 0.1;
    public static final int JAKE = 1;

    public boolean isGameStopped = true;

    public float speed = 1;
    public Sound sound;

    public boolean isNewRound = false;
    public boolean playerDidNotMoved = false;
    public double countOfRound = 0;
    public int round = 0;
    public int player_id;

    public static final int LAYER_COUNT = 3;

    public int width = 0, height = 0;

    public SceneListener sceneListener;
    public boolean isServer;

    public interface SceneListener {
        void onGameOver();

        void onNextStep();
    }

    protected float dx, dy = 0;

    public mLayer[] layers = new mLayer[LAYER_COUNT];

    protected Resources res;
    public mPlayerSprite player;
    public mLive live;
    public mPlayerSprite player2;
    public mLive live2;

    private static final Random RND = new Random();
    private static final String TAG = "mGame";

    private static int NUM_OF_ITERATION_DIV_50 = 0;
    private static final int TIME_OF_ROUND = 30;

    private static final double PAUSE_ON_SLEEP = 30;
    private static final double DELTA_SPEED = 0.1;
    private static final double DELTA_ADDING_BARRIERS = 0.2;

    private static final int FPS = 10;
    private static final int SCALE_INCREASE = 50;

    private boolean isGamePaused = true;

    private boolean isGameInitiated = false;
    private boolean wasStartRequest = false;

    private int numOfTheme = 0;

    private Timer timer;

    private int lastRound = 0;

    public mGame(Resources res, int numOfTheme, Sound sound) {
        RND.setSeed(3571);
        this.res = res;
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i] = new mLayer(i);
        }
        this.sound = sound;
        this.numOfTheme = numOfTheme;
    }

    public synchronized void start() {
        wasStartRequest = true;
        if (isGameInitiated) {
            startImpl();
        }
    }

    public synchronized void resume() {
        if (isGameInitiated) {
            resumeImpl();
        }
    }

    public synchronized void pause() {
        if (!isGamePaused) {
            Log.d(TAG, "paused");
            isGamePaused = true;
            timer.cancel();
        }
    }

    public synchronized void stop() {
        if (!isGameStopped) {
            pause();
            Log.d(TAG, "stopped");
            isGameStopped = true;
            if (sceneListener != null)
                sceneListener.onGameOver();
        }
    }

    public void setWH(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dx = (float) (-Math.sin(Math.toRadians(event.values[1])) * SCALE_INCREASE);
        dy = (float) (Math.sin(Math.toRadians(event.values[2])) * SCALE_INCREASE);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public synchronized void initGame() {
        isGameInitiated = true;
        if (wasStartRequest) {
            startImpl();
        }
    }

    protected void updateExist() {
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i].updateExist();
        }
        mBasic barrier = player.updateExist(this);
        deleteBarrier(barrier);
    }

    public abstract void restart();

    protected synchronized void add() {
        addBarrier();
        addBackground();
    }

    protected synchronized void addBarrier() {
        if (layers[0].tryToAdd()) {
            NUM_OF_ITERATION_DIV_50++;
            if (NUM_OF_ITERATION_DIV_50 >= 50) {
                NUM_OF_ITERATION_DIV_50 = 0;
            }
            mBarrierSprite barrierSprite = new mBarrierSprite(speed, numOfTheme, height, RND.nextInt(5), RND.nextInt(5));
            layers[NUM_BARRIERS_IN_LAYERS].add(barrierSprite);
        }
    }

    protected void deleteBarrier(mBasic item) {
        layers[NUM_BARRIERS_IN_LAYERS].delete(item);
    }

    protected void addBackground() {
        if (numOfTheme != 1) {
            for (int i = 1; i < 3; i++) {
                if (layers[i].tryToAdd()) {
                    mBackgroundSprite backgroundSprite = new mBackgroundSprite(speed, i == 1, numOfTheme, height);
                    layers[i].add(backgroundSprite);
                }
            }
        }
    }

    protected void recalculateNewRound() {
        if ((int) countOfRound % TIME_OF_ROUND == 0 && countOfRound > TIME_OF_ROUND) {
            newRound();
            countOfRound++;
        }
        if (lastRound < PAUSE_ON_SLEEP) {
            lastRound++;
        }
        if (lastRound == PAUSE_ON_SLEEP && isNewRound) {
            for (mLayer l : layers) {
                if (l.frequencyOfAdding > 2) {
                    l.frequencyOfAdding -= DELTA_ADDING_BARRIERS;
                }
                l.isDamaged = false;
            }
            speed += DELTA_SPEED;
            isNewRound = false;
            playerDidNotMoved = false;
        }
    }

    protected void newRound() {
        round++;
        for (mLayer l : layers) {
            l.isDamaged = true;
        }
        lastRound = 0;
        isNewRound = true;
        playerDidNotMoved = true;
    }

    protected abstract void oneStep();

    private class GameTask extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "next step");
            oneStep();
            if (sceneListener != null) {
                sceneListener.onNextStep();
            }
        }
    }

    private void startImpl() {
        if (isGameStopped) {
            Log.d(TAG, "started");
            isGameStopped = false;
            resumeImpl();
        }
    }

    private void resumeImpl() {
        if (isGamePaused) {
            Log.d(TAG, "resumed");
            timer = new Timer();
            timer.schedule(new GameTask(), 0, 1000 / FPS);
            isGamePaused = false;
        }
    }
}
