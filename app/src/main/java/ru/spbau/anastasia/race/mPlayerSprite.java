package ru.spbau.anastasia.race;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class mPlayerSprite extends mSimpleSprite {

    public static final int DAMAGED_TIME = 20;
    public static final int JUMP_TIME = 15;
    public static final int DEAD_TIME = 12;

    private int timerDamaged = 0;
    private int timerJump = 0;
    private int timerLastDead = 0;

    public boolean isJumping = false;
    public boolean isDamaged = false;

    public static final float DX = 0;
    public static final float DY = 0;

    private int step = 0;
    private int live;

    private Bitmap[] bmps;
    private Bitmap damagedBmp;
    private Bitmap jumpBmp;
    private float startX;
    private float startY;

    public mPlayerSprite(float x, float y, Resources res, int id1, int id2, int id3, int id4,
                         float height_) {

        super(x, y, DX, DY, res, id1, height_);

        startX = x;
        startY = y;

        type = TYPE_PLAYERSPRITE;

        live = 3;

        bmps = new Bitmap[2];
        bmps[0] = bmp;
        bmps[1] = BitmapFactory.decodeResource(res, id2);

        jumpBmp = BitmapFactory.decodeResource(res, id3);
        damagedBmp = BitmapFactory.decodeResource(res, id4);
    }

    public mBasic updateExist(mScene scene){

        if (timerDamaged == DAMAGED_TIME) {
            isDamaged = false;
            if (live == 0) {
                died(scene);
                scene.sound.play(Sound.LOSE);
            }
            scene.isSleeping = false;
        }

        if (isJumping || isDamaged) {
            return null;
        }

        for (mBasic a : scene.layers[0].data) {
            if (a != null && this.isSelected(a)) {
                live--;
                isDamaged = true;
                timerDamaged = 0;
                scene.sound.play(Sound.CRASH);
                scene.isSleeping = true;
                return a;
            }
        }

        return null;
    }

    public int getLive (){
        return live;
    }

    void updateStatus(boolean isSleeping) {

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
        }

        if (isJumping) {
            bmp = jumpBmp;
        } else if (isDamaged) {
            bmp = damagedBmp;
        } else {
            bmp = bmps[step];
            if (!isSleeping) {
                step = (step + 1) % 2;
            }
        }
    }

    public void startJump(Sound sound, boolean isStop) {

        if (timerLastDead < DEAD_TIME){
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

    void update(float dx, float dy) {

        if (!isJumping && !isDamaged) {
            truAddDX(dx);
            truAddDY(dy);
        }

        src.set(0, 0, bmp.getWidth(), bmp.getHeight());
    }

    public void restart() {

        x = startX;
        y = startY;

        live = 3;

        isDamaged = false;
        isJumping = false;

        timerDamaged = 0;
        timerJump = 0;

        exist = true;
    }

    private void died(mScene scene) {

        for (mLayer line : scene.layers) {
            line.clear();
            line.isDamaged = true;
        }

        scene.status = mScene.STOPED;
        exist = false;
    }

    private void truAddDX(float dx) {
        x += dx;
        if (y < (1.62 * mSettings.CurrentYRes * x
                / mSettings.CurrentXRes - 0.82 * mSettings.CurrentYRes)) {
            x -= dx;
        }
    }

    private void truAddDY(float dy) {
        if (y + dy > mSettings.CurrentXRes / 6 && y + dy < mSettings.CurrentYRes * 7 / 8) {
            y += dy;
        }
    }

    @Override
    void update() {}
}
