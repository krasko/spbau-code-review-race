package ru.spbau.anastasia.race;

import android.content.res.Resources;

public class mGameForOne extends mGame {

    public mGameForOne(Resources res, int numOfTheme, Sound sound) {
        super(res, numOfTheme, sound);
    }

    public void oneStep() {
        recalculateNewRound();
        if (!isNewRound) {
            player.updateStatus(playerDidNotMoved, this);
        }
        if (!playerDidNotMoved) {
            add();
            update();
            countOfRound += DELTA_COUNT;
        }
        updateExist();
    }

    @Override
    public synchronized void initGame() {
        mBarrierSprite.initBarrier(res);
        mBackgroundSprite.initBarrier(res);

        player = new mPlayerSprite(width / 2, height - 120 * mSettings.ScaleFactorY, res,
                (player_id == JAKE) ? R.drawable.jake1 : R.drawable.finn1,
                (player_id == JAKE) ? R.drawable.jake2 : R.drawable.finn2,
                (player_id == JAKE) ? R.drawable.jake3 : R.drawable.finn3,
                (player_id == JAKE) ? R.drawable.jake4 : R.drawable.finn4, height);

        live = new mLive(res, 1, height);
        super.initGame();
    }

    public void update() {
        for (mLayer l : layers) {
            l.update();
        }
        player.update(dx, dy);
        live.update(player);
    }

    protected void restart() {
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
        start();
    }
}
