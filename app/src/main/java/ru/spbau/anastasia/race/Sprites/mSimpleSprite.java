package ru.spbau.anastasia.race.sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import ru.spbau.anastasia.race.util.mSettings;

public abstract class mSimpleSprite extends mBasic {

    public Rect src;

    private Rect dst;

    private static final String TAG = mSimpleSprite.class.getSimpleName();
    private static final int SIZE_OF_BARRIER = 10;
    private static final int SIZE_OF_DELTA_BARRIER = 90;
    private static final int SIZE_OF_BACKGROUND = 8;
    private static final int SIZE_OF_DELTA_BACKGROUND = 100;

    private float sizeOfBarrier;
    private float deltaOfSizeOfBarrier;
    private float sizeOfBackground;
    private float deltaOfSizeOfBackgrounds;

    public mSimpleSprite(float x, float y, float dx, float dy, Bitmap bmp, float height_) {
        sizeOfBarrier = height_ / SIZE_OF_BARRIER;
        sizeOfBackground = height_ / SIZE_OF_BACKGROUND;
        deltaOfSizeOfBackgrounds = height_ / SIZE_OF_DELTA_BACKGROUND;
        deltaOfSizeOfBarrier = height_ / SIZE_OF_DELTA_BARRIER;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.bmp = bmp;
        recalculateParameters();
        src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        dst = new Rect();
        initLog();
    }

    public mSimpleSprite(float x, float y, float dx, float dy, Resources res, int id, float height_) {
        this(x, y, dx, dy, BitmapFactory.decodeResource(res, id), height_);
    }

    public boolean isSelected(mBasic player) {
        return intersect(player.x, player.y, player.getWidth(), player.getHeight());
    }

    @Override
    public void draw(Canvas c, Paint p) {
        recalculateParameters();
        if (type != TYPE_LIVE) {
            dst.set(-(int) width, -2 * (int) width, (int) width, 2 * (int) width);
        }

        dst.offset((int) x, (int) y);
        c.drawBitmap(bmp, src, dst, p);
    }

    public abstract void update();

    private void initLog() {
        if (bmp == null) {
            Log.e(TAG, "Created invalid sprite with no bitmap, width = "
                    + Integer.toString((int) width) + ", height = "
                    + Integer.toString((int) height));
        } else {
            Log.d(TAG, "Created valid sprite with bitmap = " + bmp.toString()
                    + ", width = " + Integer.toString((int) width) + ", height = "
                    + Integer.toString((int) height));
        }
    }

    private boolean intersect(float x1, float y1, float dx, float dy) {
        Rect a = new Rect((int) x1, (int) y1, (int) (x1 + dx), (int) (y1 + dy));
        Rect b = new Rect((int) x, (int) y, (int) (x + width), (int) (y + height));
        return Rect.intersects(a, b);
    }

    private void recalculateParameters() {
        switch (type) {
            case TYPE_BACKGROUND_SPRITE:
                height = y / deltaOfSizeOfBackgrounds + sizeOfBackground;
                width = height / 4;
                break;
            default:
                height = y / deltaOfSizeOfBarrier + sizeOfBarrier;
                width = height * mSettings.ScaleFactorX / 4;
                break;
        }
    }
}
