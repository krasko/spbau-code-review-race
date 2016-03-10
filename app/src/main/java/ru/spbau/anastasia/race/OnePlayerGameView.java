package ru.spbau.anastasia.race;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class OnePlayerGameView extends View {

    public mGame game;
    public boolean gameStopped = false;

    protected Paint mainPaint, textPaint;

    private Bitmap background;
    private Bitmap restart;

    public OnePlayerGameView(Context context) {
        super(context);
        init();
    }

    public OnePlayerGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void initBackground(int numOfTheme) {
        if (numOfTheme == GameMenu.IS_CHECKED) {
            background = BitmapFactory.decodeResource(getResources(), R.drawable.winter_road);
            restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart2);
        } else {
            background = BitmapFactory.decodeResource(getResources(), R.drawable.game_road_new);
            restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        synchronized (game) {
            if (!game.player.isDamaged) {
                game.player.startJump(game.sound, game.isGameStopped);
            }
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (game == null) {
            return;
        }

        game.setWH(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        mSettings.GenerateSettings(game.width, game.height);
        background = Bitmap.createScaledBitmap(background, mSettings.CurrentXRes, mSettings.CurrentYRes, false);
        restart = Bitmap.createScaledBitmap(restart, mSettings.CurrentXRes,
                mSettings.CurrentYRes, false);

        game.initGame();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        if (game == null) {
            return;
        }

        synchronized (game) {

            if (!gameStopped) {
                canvas.drawBitmap(background, 0, 0, mainPaint);

                for (mLayer l : game.layers) {
                    if (l != null) {
                        for (mBasic tmp : l.data) {
                            tmp.draw(canvas, mainPaint);
                        }
                    }
                }

                game.player.draw(canvas, mainPaint);
                game.live.draw(canvas, mainPaint);

                if (game.isNewRound) {
                    int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
                    canvas.drawText("New Round:  " + game.round, x, mSettings.CurrentYRes / 2, textPaint);
                } else {
                    int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
                    canvas.drawText(String.valueOf((int) game.countOfRound), x, mSettings.CurrentXRes / 9, textPaint);
                }
            } else {
                canvas.drawBitmap(restart, 0, 0, mainPaint);
            }
            invalidate();
        }
    }

    private void init() {
        mSettings.GenerateSettings(getWidth(), getHeight());
        background = BitmapFactory.decodeResource(getResources(), R.drawable.game_road_new);
        restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart);
        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(120f);
        textPaint.setStyle(Paint.Style.STROKE);
    }
}
