package ru.spbau.anastasia.race.Game;

import android.content.res.Resources;
import android.util.Log;

import ru.spbau.anastasia.race.FileForSent;
import ru.spbau.anastasia.race.R;
import ru.spbau.anastasia.race.Sound;
import ru.spbau.anastasia.race.Sprites.mBackgroundSprite;
import ru.spbau.anastasia.race.Sprites.mBarrierSprite;
import ru.spbau.anastasia.race.Sprites.mLive;
import ru.spbau.anastasia.race.Sprites.mPlayerSprite;
import ru.spbau.anastasia.race.mLayer;
import ru.spbau.anastasia.race.mSettings;

public class mGameForTwo extends mGame {

    private FileForSent received = null;
    private FileForSent toSend = null;

    private boolean msgToSendUpdated = false;

    public mGameForTwo(Resources res, int numOfTheme, Sound sound) {
        super(res, numOfTheme, sound);
    }

    public void oneStep() {
        recalculateNewRound();

        if (!isNewRound) {
            player.updateStatus(playerDidNotMoved, this);
            player2.updateStatus(playerDidNotMoved, this);
        }
        if (!playerDidNotMoved) {
            add();
            update();
            countOfRound += DELTA_COUNT;
        }
        updateExist();
        Log.d("oneStep", Float.toString(dx) + " " + Float.toString(dy) +
                " player : " + player.info() +
                " player2 : " + player2.info());
        toSend = new FileForSent(player.getPureX(), player.getPureY(), player.isJumping, player.justDied, player.lastBarrier);
        msgToSendUpdated = true;
    }

    @Override
    public synchronized void initGame() {
        mBarrierSprite.initBarrier(res);
        mBackgroundSprite.initBarrier(res);
        if (isServer) {
            player = new mPlayerSprite(width / 2 - 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.jake1, R.drawable.jake2,
                    R.drawable.jake3, R.drawable.jake4, height);
            live = new mLive(res, mLive.FIRST_PLAYER, height);

            player2 = new mPlayerSprite(width / 2 + 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.finn1, R.drawable.finn2,
                    R.drawable.finn3, R.drawable.finn4, height);
            live2 = new mLive(res, mLive.SECOND_PLAYER, height);
        } else {
            player2 = new mPlayerSprite(width / 2 - 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.jake1, R.drawable.jake2,
                    R.drawable.jake3, R.drawable.jake4, height);
            live2 = new mLive(res, mLive.FIRST_PLAYER, height);

            player = new mPlayerSprite(width / 2 + 60 * mSettings.ScaleFactorX,
                    height - 120 * mSettings.ScaleFactorY, res, R.drawable.finn1, R.drawable.finn2,
                    R.drawable.finn3, R.drawable.finn4, height);
            live = new mLive(res, mLive.SECOND_PLAYER, height);
        }
        super.initGame();
    }

    public void update() {
        for (mLayer l : layers) {
            l.update();
        }
        player.update(dx, dy);
        live.update(player);
        if (received != null) {
            player2.remoteUpdate(this, received);
            received = null;
        }
        live2.update(player2);
    }

    public void registerMsg(FileForSent received) {
        this.received = received;
    }

    public FileForSent getMsgToSend() {
        if (msgToSendUpdated) {
            msgToSendUpdated = false;
            return toSend;
        }
        return null;
    }

    public void restart() {
        speed = 1;
        for (mLayer l : layers) {
            l.frequencyOfAdding = 5;
        }
        isNewRound = false;
        playerDidNotMoved = false;
        countOfRound = 0;
        for (int i = 0; i < LAYER_COUNT; i++) {
            layers[i].restart();
        }
        player.restart();
        live.update();
        player2.restart();
        live2.update();
        start();
    }
}
