package ru.spbau.anastasia.race;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import ru.spbau.anastasia.race.R;

/**
 * The OnePlayerOption class provides settings for one player game mode.
 * On this activity you can choose a character you want to play for.
 */
public class OnePlayerOption extends BaseActivity {

    private ImageButton finn = null;
    private ImageButton jake = null;

    private int player_id = 0;

    /**
     * Create an activity: set background (depends on chosen theme) and set a default character (Finn).
     *
     * @param savedInstanceState default parameter for this method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_player_option);

        chooseCharacter(0);

        ImageView background = (ImageView) findViewById(R.id.imagePlayersOption);

        if (numOfTheme == GameMenu.IS_CHECKED) {
            background.setImageResource(R.drawable.one_player_option2);
        } else {
            background.setImageResource(R.drawable.one_player_option);
        }

        finn = (ImageButton) findViewById(R.id.buttonChooseFinn);
        jake = (ImageButton) findViewById(R.id.buttonChooseJake);
    }

    /**
     * Start game with chosen character in one player game mode.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonStartOnePlayer(View view) {
        startActivity(new Intent(this, RoadForOne.class).putExtra("player", player_id));
    }

    /**
     * Finish the activity and go back to the GameMenu activity.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonBackOnePlayerOption(View view) {
        finish();
    }

    /**
     * Set to the player_id the character which player have chosen.
     *
     * @param character the character which player have chosen: 0 for Finn and 1 for Jake
     */
    protected void chooseCharacter(int character) {
        this.player_id = character;
    }

    /**
     * Call the function which set player_id in Finn mode.
     * Change the pictures in the activity to make player know that he just have chosen Finn.
     *
     * @param view default parameter for this method
     */
    public void onFinnChosen(View view) {
        chooseCharacter(0);
        finn.setImageResource(R.drawable.chosen_finn);
        jake.setImageResource(R.drawable.choose_jake);
    }

    /**
     * Call the function which set player_id in Jake mode.
     * Change the pictures in the activity to make player know that he just have chosen Jake.
     *
     * @param view default parameter for this method
     */
    public void onJakeChosen(View view) {
        chooseCharacter(1);
        finn.setImageResource(R.drawable.choose_finn);
        jake.setImageResource(R.drawable.chosen_jake);
    }
}
