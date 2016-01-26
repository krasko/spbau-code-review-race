package ru.spbau.anastasia.race;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.FileReader;

public class mScene {
    public static int num = 0;

    public float speed = 1;
    public boolean isServer;
    public Sound sound;
    byte[] playerStatus = FileForSent.genServer().toMsg();

    public mBarrierSprite lastBarrier;
    public int numOfTheme = 0;
    public boolean isNewRound = false;
    public boolean isSleeping = false;

    public static final int TIME_OF_ROUND = 30;
    public static final double DELTA_COUNT = 0.1;
    public static final double PAUSE_ON_SLEEP = 30;

    public double count = 0;
    public int round = 0;
    public boolean dead = false;

    private int lastRound = 0;

    public static int[] coordinates;
    public static final int SINGLE_PLAY = 1;
    public static final int PLAY_TOGETHER = 2;
    public static final double DELTE_SPEED = 0.1;
    public static final double DELTE_ADDING_BARRIERS = 0.2;

    public static final int PLAYED= 1;
    public static final int STOPED = 2;

    public static final int FINN = 0;
    public static final int JAKE = 1;

    public int status;
    public int type;
    public int player_id;

    public static final int LAY_COUNT = 3;
    protected mLayer[] layers = new mLayer[LAY_COUNT];

    private Resources res;

    protected mPlayerSprite player;
    protected mPlayerSprite player2;
    protected mLive live;
    protected mLive live2;

    interface SceneListener {
        void onGameOver();
    }

    SceneListener sceneListener;

    public int width = 0, height = 0;

    public mScene(Resources res, int type_, int numOfTheme_, Sound sound_) {
        this.res = res;
        for (int i = 0; i < LAY_COUNT; i++) {
            layers[i] = new mLayer(i, numOfTheme);
        }
        type = type_;
        status = STOPED;
        sound = sound_;
        numOfTheme = numOfTheme_;

        coordinates = new int[100];
        for (int i = 0; i < 100; i++) {
            coordinates[i] = (i^3  + i^2 * 2 + i * 4  - 3) % 5;
        }
    }

    public synchronized void start() {
        status = PLAYED;
    }

    public synchronized void stop() {
        status = STOPED;
    }

    public synchronized void oneStep(float dx, float dy) {
        recalcNewRound();

        if (status != STOPED) {
            if (!isNewRound) {
                player.updateStatus(isSleeping);
            }
            if (!isSleeping) {
                add();
                update(dx, dy);
                count += DELTA_COUNT;
            }
            updateExist();
            recalcParametrs();
        }
    }

    private synchronized void recalcParametrs() {
        if (status == STOPED && sceneListener != null) {
            sceneListener.onGameOver();
        }
    }

    private synchronized void recalcNewRound () {
        if ((int)count % TIME_OF_ROUND == 0 && count > TIME_OF_ROUND) {
            newRound();
            count++;
        }
        if (lastRound < PAUSE_ON_SLEEP) {
            lastRound++;
        }
        if (lastRound == PAUSE_ON_SLEEP && isNewRound) {
            for (mLayer l : layers) {
                if (l.frequencyOfAdding > 2) {
                    l.frequencyOfAdding -= DELTE_ADDING_BARRIERS;
                }
                l.isDamaged = false;
            }
            speed += DELTE_SPEED;
            isNewRound = false;
            isSleeping = false;
        }
    }

    private synchronized void newRound() {
        round++;
        for (mLayer l : layers) {
            l.isDamaged = true;
        }
        lastRound = 0;
        isNewRound = true;
        isSleeping = true;
    }

    public synchronized FileForSent oneStepForTwo(float dx, float dy, FileForSent file) {
        recalcNewRound();
        FileForSent fileNew = null;

        if (status != STOPED) {
            if (!isNewRound){
                player.updateStatus(isSleeping);
                player2.updateStatus(isSleeping);
            }
            if (!isSleeping) {
                player2.isJumping = file.getIsJumping();
                addForTwo();
                update(dx, dy, file.getDX(), file.getDY());
                count += DELTA_COUNT;
            }
            updateExist();
            recalcParametrs();
            if (file != null) {
                return fileNew;
            }
            return new FileForSent(player.dx, player.dy, player.isJumping);
        }
        return null;
    }

    public synchronized void addForTwo() {
        addBackground();
        addBarrierForTwo();
    }

    public synchronized void addBarrierForTwo() {
        if (layers[0].tryToAdd()) {
            num++;
            if (num >= 50){
                num = 0;
            }
            mBarrierSprite barrierSprite = new mBarrierSprite(speed, numOfTheme, height, coordinates[2 * num], coordinates[2 * num + 1]);
            lastBarrier = barrierSprite;
            layers[0].add(barrierSprite);
        }
    }

    public synchronized void setWH(int w, int h) {
        width = w;
        height = h;
    }

    public synchronized void initScene() {
        switch (type) {
            case SINGLE_PLAY :
                initSingleScene();
                break;
            case PLAY_TOGETHER :
                initDoubleScene();
                break;
        }
    }

    public synchronized void initSingleScene() {
        mBarrierSprite.initBarrier(res);
        mBackgroundSprite.initBarrier(res);

        player = new mPlayerSprite(width/2, height - 120 * mSettings.ScaleFactorY, res,
                (player_id == JAKE) ? R.drawable.jake1 : R.drawable.finn1,
                (player_id == JAKE) ? R.drawable.jake2 : R.drawable.finn2,
                (player_id == JAKE) ? R.drawable.jake3 : R.drawable.finn3,
                (player_id == JAKE) ? R.drawable.jake4 : R.drawable.finn4, height);

        live = new mLive(res, SINGLE_PLAY, height);
    }

    public synchronized void initDoubleScene() {
        mBarrierSprite.initBarrier(res);
        mBackgroundSprite.initBarrier(res);
        if (isServer){
            player = new mPlayerSprite(width/2 - 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.jake1, R.drawable.jake2,
                    R.drawable.jake3, R.drawable.jake4, height);
            live = new mLive(res, mLive.FIRST_PLAYER, height);

            player2 = new mPlayerSprite(width/2 + 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.finn1, R.drawable.finn2,
                    R.drawable.finn3, R.drawable.finn4, height);
            live2 = new mLive(res, mLive.SECOND_PLAYER, height);
        } else {
            player2 = new mPlayerSprite(width/2 - 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.jake1, R.drawable.jake2,
                    R.drawable.jake3, R.drawable.jake4, height);
            live2 = new mLive(res, mLive.FIRST_PLAYER, height);

            player = new mPlayerSprite(width/2 + 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.finn1, R.drawable.finn2,
                    R.drawable.finn3, R.drawable.finn4, height);
            live = new mLive(res, mLive.SECOND_PLAYER, height);
        }

    }

    public synchronized void add() {
        addBarrier();
        addBackground();
    }

    public synchronized void addBarrier() {
        if (layers[0].tryToAdd()) {
            num++;
            if (num >= 50){
                num = 0;
            }
            mBarrierSprite barrierSprite = new mBarrierSprite(speed, numOfTheme, height, coordinates[2 * num], coordinates[2 * num + 1]);
            layers[0].add(barrierSprite);
        }
    }


    public synchronized void deleteBarrier(mBasic item) {
        layers[0].delete(item);
    }

    public synchronized void addBackground() {
        if (numOfTheme != 1) {
            for (int i = 1; i < 3; i++) {
                if (layers[i].tryToAdd()) {
                    mBackgroundSprite backgroundSprite = new mBackgroundSprite(speed, i == 1, numOfTheme, height);
                    layers[i].add(backgroundSprite);
                }
            }
        }
    }

    public synchronized void updateExist() {
        for (int i = 0; i < LAY_COUNT; i++) {
            layers[i].updateExist();
        }
        mBasic barrier = player.updateExist(this);
        deleteBarrier(barrier);
        live.update();

        if (type == PLAY_TOGETHER) {
            mBasic barrier2 = player2.updateExist(this);
            deleteBarrier(barrier2);
            live2.update();
        }
    }

    public synchronized void restart() {
        speed = 1;
        for (mLayer l : layers) {
            l.frequencyOfAdding = 5;
        }
        isNewRound = false;
        isSleeping = false;
        count = 0;
        for (int i = 0; i < LAY_COUNT; i++) {
            layers[i].restart();
        }
        player.restart();
        live.update();
        if (type != SINGLE_PLAY) {
            player2.restart();
            live2.update();
        }
        status = PLAYED;
    }

    public synchronized void update(float dx, float dy) {
        for (mLayer l : layers) {
            l.update();
        }
        player.update(dx, dy);
        live.update(player);
    }

    public synchronized void update(float dx, float dy, float dx2, float dy2) {
        update(dx, dy);
        if(type == PLAY_TOGETHER){
            player2.update(dx2, dy2);
            live2.update(player2);
        }
    }
}
