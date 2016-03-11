package ru.spbau.anastasia.race;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ru.spbau.anastasia.race.R;

/**
 * The GameAbout class only provides information about this project and it's authors.
 */
public class GameAbout extends BaseActivity {

    /**
     * Create activity, set background (depends on chosen theme) and text with project's description.
     *
     * @param savedInstanceState default parameter for this method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_about);

        TextView aboutTextView = (TextView) findViewById(R.id.gameAbout);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());

        ImageView backGroundImage = (ImageView) findViewById(R.id.imageGameAbout);

        if (numOfTheme == GameMenu.IS_CHECKED) {
            backGroundImage.setImageResource(R.drawable.game_about2);
        } else {
            backGroundImage.setImageResource(R.drawable.game_about);
        }
    }

    /**
     * Finish the activity and go back to the GameMenu activity.
     *
     * @param view default parameter for this method
     */
    public void onClickButtonBackGameAbout(View view) {
        finish();
    }
}
