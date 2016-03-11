package ru.spbau.anastasia.race.Sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import ru.spbau.anastasia.race.FileForSent;
import ru.spbau.anastasia.race.Game.mGame;
import ru.spbau.anastasia.race.Sound;
import ru.spbau.anastasia.race.mLayer;
import ru.spbau.anastasia.race.mSettings;

public class mPlayerSprite extends mSimpleSprite {

    public boolean isJumping = false;
    public boolean isDamaged = false;
    public boolean justDied = false;

    public int lastBarrier;

    private static final int DAMAGED_TIME = 20;
    private static final int JUMP_TIME = 15;
    private static final int DEAD_TIME = 12;

    private int timerDamaged = 0;
    private int timerJump = 0;
    private int timerLastDead = 0;

    private static final float DX = 0;
    private static final float DY = 0;

    private int step = 0;
    private int live;

    private Bitmap[] bitmaps;
    private Bitmap damagedBmp;
    private Bitmap jumpBmp;
    private float startX;
    private float startY;

    public mPlayerSprite(float x, float y, Resources res, int id1, int id2, int id3, int id4,
                         float height_) {

        super(x, y, DX, DY, res, id1, height_);

        startX = x;
        startY = y;

        type = TYPE_PLAYER_SPRITE;

        live = 3;

        bitmaps = new Bitmap[2];
        bitmaps[0] = bmp;
        bitmaps[1] = BitmapFactory.decodeResource(res, id2);

        jumpBmp = BitmapFactory.decodeResource(res, id3);
        damagedBmp = BitmapFactory.decodeResource(res, id4);
    }

    public mBasic updateExist(mGame game) {
        justDied = false;
        lastBarrier = 0;
        if (live == 0) {
            dying(game);
            game.sound.play(Sound.LOSE);
        }
        if (timerDamaged == DAMAGED_TIME) {
            isDamaged = false;
            game.playerDidNotMoved = false;
        }
        if (isJumping || isDamaged) {
            return null;
        }
        for (mBasic a : game.layers[0].data) {
            if (a != null && isSelected(a)) {
                live--;
                justDied = true;
                lastBarrier = game.layers[mGame.NUM_BARRIERS_IN_LAYERS].data.indexOf(a);
                isDamaged = true;
                timerDamaged = 0;
                game.sound.play(Sound.CRASH);
                game.playerDidNotMoved = true;
                return a;
            }
        }
        return null;
    }

    public int getLive() {
        return live;
    }

    public void updateStatus(boolean isSleeping, mGame game) {

        if (timerJump < JUMP_TIME) {
            timerJump++;
        } else if (timerJump == JUMP_TIME && isJumping) {
            isJumping = false;
            y += mSettings.CurrentYRes / 30;
            timerJump++;
        }

        if (timerLastDead <= DEAD_TIME) {
            timerLastDead++;
        }

        if (timerDamaged <= DAMAGED_TIME) {
            timerDamaged++;
        } else {
            isDamaged = false;
            game.playerDidNotMoved = false;
        }

        if (isJumping) {
            bmp = jumpBmp;
        } else if (isDamaged) {
            bmp = damagedBmp;
        } else {
            bmp = bitmaps[step];
            if (!isSleeping) {
                step = (step + 1) % 2;
            }
        }
    }

    public void startJump(Sound sound, boolean isStop) {

        if (timerLastDead < DEAD_TIME) {
            return;
        }

        timerLastDead = 0;
        timerJump = 0;
        isJumping = true;

        if (!isStop) {
            sound.play(Sound.JUMP);
        }

        y -= mSettings.CurrentYRes / 30;
    }

    public void update(float dx, float dy) {

        if (!isJumping && !isDamaged) {
            addDX(dx);
            addDY(dy);
        }

        src.set(0, 0, bmp.getWidth(), bmp.getHeight());
    }

    public void remoteUpdate(mGame game, FileForSent data) {
        this.x = data.getX();
        this.y = data.getY();
        this.isJumping = data.getIsJumping();
        if (live == 0) {
            dying(game);
            game.sound.play(Sound.LOSE);
        }
        if (timerDamaged == DAMAGED_TIME) {
            isDamaged = false;
            game.playerDidNotMoved = false;
        }
        if (data.isDied) {
            live--;
            isDamaged = true;
            timerDamaged = 0;
            game.sound.play(Sound.CRASH);
            game.playerDidNotMoved = true;
            game.layers[mGame.NUM_BARRIERS_IN_LAYERS].data.remove(data.numOfBarrier);
        }
    }

    public float getPureX() {
        return x / mSettings.CurrentXRes;
    }

    public float getPureY() {
        return y / mSettings.CurrentYRes;
    }

    public void restart() {

        x = startX;
        y = startY;

        live = 3;
        isDamaged = false;
        isJumping = false;
        timerDamaged = 0;
        timerJump = 0;
        exists = true;
    }

    public String info() {
        return String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(isJumping);
    }

    @Override
    public void update() {
    }

    private void dying(mGame game) {

        for (mLayer line : game.layers) {
            line.clear();
            line.isDamaged = true;
        }
        game.stop();
        exists = false;
    }

    private void addDX(float dx) {
        this.dx = dx;
        if (y < (1.62 * mSettings.CurrentYRes * x
                / mSettings.CurrentXRes - 0.82 * mSettings.CurrentYRes)) {
            this.dx = 0;
        }
        this.x += this.dx;
    }

    private void addDY(float dy) {
        this.dy = 0;
        if (y + dy > mSettings.CurrentXRes / 6 && y + dy < mSettings.CurrentYRes * 7 / 8) {
            this.dy = dy;
        }
        this.y += this.dy;
    }
}
