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

    protected mScene scene;
    protected Paint mainPaint, textPaint;
    protected Bitmap fon;
    protected Bitmap restart;

    public OnePlayerGameView(Context context) {
        super(context);
        init();
    }

    public OnePlayerGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSettings.GenerateSettings(getWidth(), getHeight());
        fon = BitmapFactory.decodeResource(getResources(), R.drawable.game_road);
        restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart);
        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(120f);
        textPaint.setStyle(Paint.Style.STROKE);
    }

    public void initFon(int numOfTheme) {
        if (numOfTheme == GameMenu.IS_CHECKED) {
            fon = BitmapFactory.decodeResource(getResources(), R.drawable.winter_road);
            restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart2);
        } else {
            fon = BitmapFactory.decodeResource(getResources(), R.drawable.game_road);
            restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (scene == null) {
            return;
        }

        scene.setWH(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        mSettings.GenerateSettings(scene.width, scene.height);
        fon = Bitmap.createScaledBitmap(fon, mSettings.CurrentXRes, mSettings.CurrentYRes, false);
        restart = Bitmap.createScaledBitmap(restart, mSettings.CurrentXRes,
                mSettings.CurrentYRes, false);

        scene.initScene();
        scene.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (scene) {
            if (scene == null) {
                return;
            }
            if (scene.status == mScene.PLAYED) {
                canvas.drawBitmap(fon, 0, 0, mainPaint);

                for (mLayer l : scene.layers) {
                    if (l != null) {
                        for (mBasic tmp : l.data) {
                            tmp.draw(canvas, mainPaint);
                        }
                    }
                }

                scene.player.draw(canvas, mainPaint);
                scene.live.draw(canvas, mainPaint);

                if (scene.isNewRound) {
                    int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
                    canvas.drawText("New Round:  " + scene.round, x, mSettings.CurrentYRes / 2, textPaint);
                } else {
                    int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
                    canvas.drawText(String.valueOf((int) scene.count), x, mSettings.CurrentXRes / 9, textPaint);
                }

            } else {
                canvas.drawBitmap(restart, 0, 0, mainPaint);

                if (scene.dead) {
                    double newScore = (int)scene.count;

                    DataBaseHelper mDatabaseHelper = new DataBaseHelper(getContext(), "best_scores.db", null, 1);
                    SQLiteDatabase mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

                    Cursor cursor = mSqLiteDatabase.query("Scores", new String[]{DataBaseHelper.SCORE_COLUMN},
                            null, null, null, null, null) ;

                    cursor.moveToLast();

                    int bestScore = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.SCORE_COLUMN));

                    cursor.close();

                    if (newScore > bestScore) {
                        ContentValues newValues = new ContentValues();
                        newValues.put(DataBaseHelper.SCORE_COLUMN, newScore);
                        mSqLiteDatabase.insert("Scores", null, newValues);
                    }

                    Toast toast = Toast.makeText(getContext(), "Your score: " +
                            newScore + "; last best score: " + bestScore, Toast.LENGTH_SHORT);
                    toast.show();

                    scene.dead = false;
                }
            }
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }

        synchronized (scene) {
            if (!scene.player.isDamaged) {
                scene.player.startJump(scene.sound, scene.status == mScene.STOPED);
            }
        }
        return true;
    }
}
