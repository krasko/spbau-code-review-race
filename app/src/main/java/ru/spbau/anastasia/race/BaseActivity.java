package ru.spbau.anastasia.race;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * A basic class for all activities in the game.
 */
public class BaseActivity extends Activity {

    /**
     * numOfTheme contains information about which theme is chosen right now.
     * By default, the theme is the original one.
     * When sound is turned on the isSound is True and False else.
     */
    protected int numOfTheme = 0;
    protected boolean isSound = false;

    /**
     * Start activity with chosen theme and enabled or disabled sound.
     *
     * @param intent default parameter for this method
     */
    @Override
    public void startActivity(Intent intent) {
        intent.putExtra("theme", numOfTheme);
        intent.putExtra("sound", isSound);
        super.startActivity(intent);
    }

    /**
     * Start activity with chosen theme, enabled or disabled sound and some additional information.
     *
     * @param intent default parameter for this method
     * @param requestCode default parameter for this method
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra("theme", numOfTheme);
        intent.putExtra("sound", isSound);
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Create an activity and set theme and sound for it (depends on settings which player
     * can choose in the GameMenu activity).
     *
     * @param savedInstanceState default parameter for this method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            numOfTheme = extras.getInt("theme");
            isSound = extras.getBoolean("sound");
        }
    }
}
