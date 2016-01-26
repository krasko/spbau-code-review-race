package ru.spbau.anastasia.race;

public class mSettings {

	public static int DefaultXRes = 800;
	public static int DefaultYRes = 480;
	public static int CurrentXRes;
	public static int CurrentYRes;
	public static float ScaleFactorX = 1;
	public static float ScaleFactorY = 1;
	
	public static void GenerateSettings(int w, int h) {

		mSettings.CurrentXRes = w;
		mSettings.CurrentYRes = h;

		mSettings.ScaleFactorX = mSettings.CurrentXRes/(float)mSettings.DefaultXRes;
		mSettings.ScaleFactorY = mSettings.CurrentYRes/(float)mSettings.DefaultYRes;
	}

}
