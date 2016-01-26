package ru.spbau.anastasia.race;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

public class GameMenu extends Activity {

    private CheckBox chooseWinterTheme;
    private ImageButton soundButton;
    private ImageView backgroundImage;

    private MediaPlayer mediaPlayer;

    public static final int IS_CHECKED = 1;
    public static final int NOT_IS_CHECKED = 0;
    public static final int MENU_ACTIVITY = 100;

    private int numOfTheme = 0;
    private boolean isSound;
    private Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        chooseWinterTheme = (CheckBox) findViewById(R.id.winter);
        backgroundImage = (ImageView) findViewById(R.id.imageGameMenu);

        soundButton = (ImageButton) findViewById(R.id.buttonSound);
        isSound = true;
        numOfTheme = NOT_IS_CHECKED;
        sound = new Sound(getAssets(), numOfTheme, MENU_ACTIVITY);
        sound.isStopped = !isSound;

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.race);
        mediaPlayer.start();

        DataBaseHelper mDatabaseHelper = new DataBaseHelper(this, "best_scores.db", null, 1);

        SQLiteDatabase mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(DataBaseHelper.SCORE_COLUMN, "0");
        mSqLiteDatabase.insert("Scores", null, newValues);
    }

    public void onClickButtonOnePlayerOption(View view) {
        startActivity(new Intent(GameMenu.this, OnePlayerOption.class));
    }

    public void onClickChooseWinterTheme(View view) {
        if (chooseWinterTheme.isChecked()) {
            backgroundImage.setImageResource(R.drawable.menu2);
            numOfTheme = IS_CHECKED;
        } else {
            backgroundImage.setImageResource(R.drawable.menu);
            numOfTheme = NOT_IS_CHECKED;
        }
        sound.theme = numOfTheme;
    }

    public void onClickSound(View view) {
        if (isSound) {
            soundButton.setImageResource(R.drawable.no_sound);
        } else {
            soundButton.setImageResource(R.drawable.sound);
        }

        mediaPlayer.stop();

        isSound = !isSound;
        sound.isStopped = !isSound;
    }

    public void onClickButtonTConnection(View view) {
        startActivity(new Intent(GameMenu.this, ConnectionGame.class));
    }

    public void onClickButtonGameInfo(View view) {
        startActivity(new Intent(GameMenu.this, GameInfo.class));
    }

    public void onClickButtonGameAbout(View view) {
        startActivity(new Intent(GameMenu.this, GameAbout.class));
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra("theme", numOfTheme);
        intent.putExtra("sound", isSound);
        super.startActivity(intent);
    }
}
