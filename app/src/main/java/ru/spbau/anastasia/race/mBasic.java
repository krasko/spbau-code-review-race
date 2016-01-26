package ru.spbau.anastasia.race;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class mBasic {

	protected Bitmap bmp;
	protected int type;
	protected boolean exist = true;

	protected float x, y;
	protected float dx, dy;
	protected float width, height;

	public static final int TYPE_PLAYERSPRITE = 1;
	public static final int TYPE_BARRIERSPRITE = 2;
	public static final int TYPE_BACKGROUNDSPRITE = 3;
	public static final int TYPE_LIVE = 4;

	abstract void update();

	abstract boolean isSelected(mBasic player);
	
	abstract void draw(Canvas c, Paint p);

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
}
