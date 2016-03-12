package ru.spbau.anastasia.race;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import ru.spbau.anastasia.race.Other.DataBaseHelper;
import ru.spbau.anastasia.race.Other.Sound;

/**
 * The GameMenu class is the main activity where everything starts.
 * From this activity you can choose game mode (one or two players mode),
 * go to have a look at the rules and some information about creators,
 * choose game theme (original and winter) and turn the sound on/off.
 */
public class GameMenu extends BaseActivity {

    /**
     * First two integers mean which theme is chosen:
     * IS_CHECKED for winter and other one for original theme.
     * Last integer is used in class Sound to restart the background music
     * (if we create Sound from GameMenu activity).
     */
    public static final int IS_CHECKED = 1;
    public static final int NOT_IS_CHECKED = 0;
    public static final int MENU_ACTIVITY = 100;

    /**
     * CheckBox to choose theme,
     * ImageButton to manager sounds,
     * ImageView to set the background (it depends on theme).
     */
    private CheckBox chooseWinterTheme = null;
    private ImageButton soundButton = null;
    private ImageView backgroundImage = null;

    /**
     * MediaPlayer controls sound in the game.
     * Sound contains all information about what and when should be played.
     */
    private MediaPlayer mediaPlayer = null;
    private Sound sound = null;

    /**
     * Create an activity:
     * Set all images in activity,
     * Set sound,
     * Create DataBase for this game session.
     *
     * @param savedInstanceState default parameter for this method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DataBaseHelper.initializeDataBase(this);

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
    }

    /**
     * Go to activity with settings for one player game mode.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonOnePlayerOption(View view) {
        startActivity(new Intent(GameMenu.this, OnePlayerOption.class));
    }

    /**
     * Change theme (including almost all images and sounds).
     *
     * @param view default parameter for this method
     */
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

    /**
     * Turn the sound off.
     *
     * @param view default parameter for this method
     */
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

    /**
     * Go to activity with settings for two player game mode.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonTConnection(View view) {
        startActivity(new Intent(GameMenu.this, DeviseChooser.class));
    }

    /**
     * Go to activity with game's rules.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonGameInfo(View view) {
        startActivity(new Intent(GameMenu.this, GameInfo.class));
    }

    /**
     * Go to activity with game developers' information.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonGameAbout(View view) {
        startActivity(new Intent(GameMenu.this, GameAbout.class));
    }
}
