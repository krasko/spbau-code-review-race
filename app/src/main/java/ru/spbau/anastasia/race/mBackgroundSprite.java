package ru.spbau.anastasia.race;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class mBackgroundSprite extends mSimpleSprite {

    private static Bitmap[][] backgroundSprite = new Bitmap[2][2];
    private static int row;
    private static float[] rowX = new float[2];
    private static float[] rowY = new float[2];
    private static float[] rowDX = new float[2];
    private static float[] rowDY = new float[2];

    public mBackgroundSprite(float speed_, boolean isLeft_,
                             int numOfTheme_, float height_) {
        super(rowX[row], rowY[row], rowDX[row] * speed_, rowDY[row] * speed_,
                withBarrier(numOfTheme_, isLeft_), height_);

        type = TYPE_BACKGROUNDSPRITE;
    }

    public static void initBarrier(Resources res) {

        backgroundSprite[0][0] = BitmapFactory.decodeResource(res, R.drawable.background1);
        backgroundSprite[0][1] = BitmapFactory.decodeResource(res, R.drawable.background1);
        backgroundSprite[1][0] = BitmapFactory.decodeResource(res, R.drawable.background1);
        backgroundSprite[1][1] = BitmapFactory.decodeResource(res, R.drawable.background1);

        for (int i = 0; i < 2; i++) {
            rowX[i] = mSettings.ScaleFactorX * (330 + i * 140);
            rowY[i] = 80 * mSettings.ScaleFactorY;
            rowDY[i] = 4 * mSettings.ScaleFactorY;
            rowDX[i] = (-37 + i * 68) * mSettings.ScaleFactorX;
        }
    }

    private static Bitmap withBarrier(int numOfTheme, boolean isLeft_){
        row = isLeft_ ? 1 : 0;
        return backgroundSprite[numOfTheme][row];
    }

    private void updateExist(){
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
