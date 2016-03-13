package ru.spbau.anastasia.race;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import ru.spbau.anastasia.race.game.mGame;
import ru.spbau.anastasia.race.game.mGameForOne;

public class RoadForOne extends BaseRoad implements mGame.SceneListener {

    private ImageButton pause, restart;
    private View.OnClickListener onPauseListener, onResumeListener;

    private Runnable activateRestartButton = new Runnable() {
        @Override
        public void run() {
            restart.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
        }
    };

    @Override
    public void onGameOver() {
        super.onGameOver();
        gameView.gameStopped = true;
        runOnUiThread(activateRestartButton);
    }

    @Override
    public void onNextStep() {
    }

    public void onRestartButtonClick(View view) {
        game.restart();
        gameView.invalidate();
        restart.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int player_id = getIntent().getExtras().getInt("player");
        setContentView(R.layout.activity_road_for_one);

        gameView = (OnePlayerGameView) findViewById(R.id.game_view);
        gameView.initBackground(numOfTheme);

        pause = (ImageButton) findViewById(R.id.pause);
        restart = (ImageButton) findViewById(R.id.restart);

        game = new mGameForOne(getResources(), numOfTheme, sound);
        synchronized (game) {
            game.player_id = player_id;
            gameView.game = game;
            game.sceneListener = this;

            game.start();
        }

        onPauseListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.pause();
                pause.setImageResource(R.drawable.play);
                pause.setOnClickListener(onResumeListener);
            }
        };

        onResumeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game.resume();
                pause.setImageResource(R.drawable.pause);
                pause.setOnClickListener(onPauseListener);
            }
        };

        restart.setVisibility(View.GONE);
        pause.setOnClickListener(onPauseListener);
    }

    public void onBackButtonClickRoadForOne(View view) {
        finish();
    }
}
