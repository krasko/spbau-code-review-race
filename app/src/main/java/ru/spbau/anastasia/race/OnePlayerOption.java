package ru.spbau.anastasia.race;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class OnePlayerOption extends BaseActivity {

    private ImageButton finn;
    private ImageButton jake;

    private int player_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_player_option);

        chooseCharacter(0);

        ImageView fon = (ImageView) findViewById(R.id.imagePlayersOption);

        if (numOfTheme == GameMenu.IS_CHECKED) {
            fon.setImageResource(R.drawable.one_player_option2);
        } else {
            fon.setImageResource(R.drawable.one_player_option);
        }

        finn = (ImageButton) findViewById(R.id.buttonChooseFinn);
        jake = (ImageButton) findViewById(R.id.buttonChooseJake);
    }

    public void onClickButtonStartOnePlayer(View view) {
        startActivity(new Intent(this, RoadForOne.class).putExtra("player", player_id));
    }

    public void onClickButtonBackOnePlayerOption(View view) {
        finish();
    }

    protected void chooseCharacter(int character) {
        this.player_id = character;
    }

    public void onFinnChosen(View view) {
        chooseCharacter(0);
        finn.setImageResource(R.drawable.chosen_finn);
        jake.setImageResource(R.drawable.choose_jake);
    }

    public void onJakeChosen(View view) {
        chooseCharacter(1);
        finn.setImageResource(R.drawable.choose_finn);
        jake.setImageResource(R.drawable.chosen_jake);
    }
}
