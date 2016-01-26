package ru.spbau.anastasia.race;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public abstract class mSimpleSprite extends mBasic{

	public static final String TAG = mSimpleSprite.class.getSimpleName();
	public static final int SIZE_OF_BARRIER = 30;
	public static final int SIZE_OF_DELTA_BARRIER = 80;
	public static final int SIZE_OF_BACKGROUND = 8;
	public static final int SIZE_OF_DELTA_BACKGROUND = 100;

	public float sizeOfBarrier;
	public float delteOfSizeOfBarrier;
	public float sizeOfBackgroun;
	public float delteOfSizeOfBackgroun;

	Rect src, dst;

	public mSimpleSprite(float x, float y, float dx, float dy, Bitmap bmp, float height_) {
		sizeOfBarrier = height_ / SIZE_OF_BARRIER;
		sizeOfBackgroun = height_ / SIZE_OF_BACKGROUND;
		delteOfSizeOfBackgroun = height_ / SIZE_OF_DELTA_BACKGROUND;
		delteOfSizeOfBarrier = height_ / SIZE_OF_DELTA_BARRIER;
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.bmp = bmp;
		recalcParametrs();
		src = new Rect(0,0, bmp.getWidth(), bmp.getHeight());
		dst = new Rect();
		initLog();
	}

	public mSimpleSprite(float x, float y, float dx, float dy, Resources res, int id, float height_) {
		this(x, y, dx, dy, BitmapFactory.decodeResource(res, id), height_);
	}

	private void initLog() {
		if (bmp == null) {
			Log.e(TAG, "Created invalid sprite with no bitmap, width = " + Integer.toString( (int) width) + ", height = " + Integer.toString( (int) height));
		} else {
			Log.d(TAG, "Created valid sprite with bitmap = " + bmp.toString() + ", width = " + Integer.toString( (int) width) + ", height = " + Integer.toString( (int) height));
		}
	}

	abstract void update();

	private boolean intersect( float x1, float y1, float dx, float dy){
		Rect a = new Rect((int) x1, (int) y1, (int) (x1 + dx), (int) (y1 + dy));
		Rect b = new Rect((int) x, (int) y, (int) (x + width), (int) (y + height));
		return Rect.intersects(a, b);
	}

	public boolean isSelected(mBasic player) {
		return intersect(player.x, player.y, player.getWidth(), player.getHeight());
	}


	private void recalcParametrs(){
		switch (type) {
			case TYPE_BACKGROUNDSPRITE:
				height = y / delteOfSizeOfBackgroun + sizeOfBackgroun;
				width = height / 4;
				break;
			default:
				height = y / delteOfSizeOfBarrier + sizeOfBarrier;
				width = height * mSettings.ScaleFactorX / 4;
				break;
		}
	}

	@Override
	public void draw(Canvas c, Paint p)
	{
		recalcParametrs();
		if (type != TYPE_LIVE) {
			dst.set(- (int) width, -2 * (int) width, (int) width, 2 * (int) width);
		}

		dst.offset((int)x, (int)y);
		c.drawBitmap(bmp, src, dst, p);
	}
}
