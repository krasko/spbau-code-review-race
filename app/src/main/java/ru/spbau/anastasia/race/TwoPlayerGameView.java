package ru.spbau.anastasia.race;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class TwoPlayerGameView extends OnePlayerGameView {

    public TwoPlayerGameView(Context context) {
        super(context);
    }

    public TwoPlayerGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (scene){
            if (scene.status == mScene.PLAYED){
                scene.player2.draw(canvas, mainPaint);
                scene.live2.draw(canvas, mainPaint);
            }
        }
        invalidate();
    }
}
