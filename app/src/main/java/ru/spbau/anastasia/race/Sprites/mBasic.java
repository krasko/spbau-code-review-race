package ru.spbau.anastasia.race.Sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class mBasic {

    public static final int TYPE_PLAYER_SPRITE = 1;
    public static final int TYPE_BARRIER_SPRITE = 2;
    public static final int TYPE_BACKGROUND_SPRITE = 3;
    public static final int TYPE_LIVE = 4;

    protected Bitmap bmp;
    protected int type;
    public boolean exists = true;

    protected float x, y;
    protected float dx, dy;
    protected float width, height;

    public abstract void update();

    abstract boolean isSelected(mBasic player);

    public abstract void draw(Canvas c, Paint p);

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
