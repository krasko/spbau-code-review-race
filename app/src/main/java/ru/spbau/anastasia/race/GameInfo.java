package ru.spbau.anastasia.race;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The GameInfo class only provides information about rules of the game.
 */
public class GameInfo extends BaseActivity {

    /**
     * Create activity, set background (depends on chosen theme) and text with game's description.
     *
     * @param savedInstanceState default parameter for this method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info);

        TextView rulesTextView = (TextView) findViewById(R.id.gameInfo);
        rulesTextView.setMovementMethod(new ScrollingMovementMethod());

        ImageView backgroundImage = (ImageView) findViewById(R.id.imageGameInfo);

        if (numOfTheme == GameMenu.IS_CHECKED) {
            backgroundImage.setImageResource(R.drawable.info2);
        } else {
            backgroundImage.setImageResource(R.drawable.info);
        }
    }

    /**
     * Finish the activity and go back to the GameMenu activity.
     *
     * @param view default parameter for this method
     */
    protected void onClickButtonBackGameInfo(View view) {
        finish();
    }
}
