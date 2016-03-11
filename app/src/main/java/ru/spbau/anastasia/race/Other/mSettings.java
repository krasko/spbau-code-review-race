package ru.spbau.anastasia.race.Other;

public class mSettings {

    public static int CurrentXRes;
    public static int CurrentYRes;
    public static float ScaleFactorX = 1;
    public static float ScaleFactorY = 1;

    private static int DefaultXRes = 800;
    private static int DefaultYRes = 480;

    public static void GenerateSettings(int w, int h) {

        mSettings.CurrentXRes = w;
        mSettings.CurrentYRes = h;

        mSettings.ScaleFactorX = mSettings.CurrentXRes / (float) mSettings.DefaultXRes;
        mSettings.ScaleFactorY = mSettings.CurrentYRes / (float) mSettings.DefaultYRes;
    }

}
