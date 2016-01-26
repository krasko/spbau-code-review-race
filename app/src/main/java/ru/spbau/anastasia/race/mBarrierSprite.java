package ru.spbau.anastasia.race;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Random;

public class mBarrierSprite extends mSimpleSprite {

    private static Bitmap [][] barriersSprite = new Bitmap[2][6];
    public static int row = 1;
    private static float[] rowX = new float[5];
    private static float[] rowY = new float[5];
    private static float[] rowDX = new float[5];
    private static float[] rowDY = new float[5];
    public static int numOfImage;

    public static final Random RND = new Random();

    public mBarrierSprite(float speed_, int numOfTheme_, float height_, int row_, int numOfImage_) {
        super(rowX[row_], rowY[row_], rowDX[row_] * speed_, rowDY[row_] * speed_,
                withBarrier(numOfTheme_, numOfImage_), height_);

        row = row_;
        numOfImage = numOfImage_;
        Log.d("server", "row: " + row + "; numOfImage");
        this.type = TYPE_BARRIERSPRITE;
    }

    public static void initBarrier(Resources res){

        barriersSprite[0][0] = BitmapFactory.decodeResource(res, R.drawable.barrier6);
        barriersSprite[0][1] = BitmapFactory.decodeResource(res, R.drawable.barrier1);
        barriersSprite[0][2] = BitmapFactory.decodeResource(res, R.drawable.barrier2);
        barriersSprite[0][3] = BitmapFactory.decodeResource(res, R.drawable.barrier3);
        barriersSprite[0][4] = BitmapFactory.decodeResource(res, R.drawable.barrier4);
        barriersSprite[0][5] = BitmapFactory.decodeResource(res, R.drawable.barrier5);

        for (int i = 0; i < 6; i++) {
            barriersSprite[1][i] = BitmapFactory.decodeResource(res, R.drawable.barrier0);
        }

        for (int i = 0; i < 5; i++) {
            rowX[i] = mSettings.ScaleFactorX * (360 + i * 20);
            rowY[i] = 90 * mSettings.ScaleFactorY;
            rowDY[i] = 14 * mSettings.ScaleFactorY;
            rowDX[i] = (-12 + i * 6) * mSettings.ScaleFactorX;
        }
    }

    private static Bitmap withBarrier(int numOfTheme, int num) {
        row = num % 5;
        numOfImage = num % 5;
        return barriersSprite[numOfTheme][numOfImage];
    }

    private void updateExist() {
        this.exist = (this.y < mSettings.CurrentYRes) && (this.x < mSettings.CurrentXRes)
                && (this.x > 0);
    }

    @Override
    void update() {
        x = x + dx;
        y = y + dy;
        updateExist();
    }
}
