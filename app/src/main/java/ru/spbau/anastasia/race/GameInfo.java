package ru.spbau.anastasia.race;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GameInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info);

        TextView rulesTextView = (TextView) findViewById(R.id.gameInfo);
        rulesTextView.setMovementMethod(new ScrollingMovementMethod());

        ImageView backgroundImage = (ImageView) findViewById(R.id.imageGameInfo);
        int numOfTheme = getIntent().getExtras().getInt("theme");

        if (numOfTheme == GameMenu.IS_CHECKED) {
            backgroundImage.setImageResource(R.drawable.info2);
        } else {
            backgroundImage.setImageResource(R.drawable.info);
        }
    }

    public void onClickButtonBackGameInfo(View view) {
        finish();
    }
}
