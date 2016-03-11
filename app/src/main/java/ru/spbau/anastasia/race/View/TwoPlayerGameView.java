package ru.spbau.anastasia.race.View;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import ru.spbau.anastasia.race.Other.mSettings;

public class TwoPlayerGameView extends OnePlayerGameView {

    public final Info playerInfo = new Info();
    public final Info deathInfo = new Info();

    public static class Info {
        private volatile boolean show = false;

        public boolean isShown() {
            return show;
        }

        public synchronized void show(final int time) {
            show = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    show = false;
                }
            }).start();
        }
    }

    public TwoPlayerGameView(Context context) {
        super(context);
    }

    public TwoPlayerGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!gameStopped) {
            game.player2.draw(canvas, mainPaint);
            game.live2.draw(canvas, mainPaint);
        }

        if (playerInfo.isShown()) {
            int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
            canvas.drawText(game.isServer ? "You're Jake" : "You're Finn", x, mSettings.CurrentYRes / 2 - 50, textPaint);
        }

        if (deathInfo.isShown()) {
            int x = (int) ((canvas.getWidth() / 2) - ((mainPaint.descent() + mainPaint.ascent()) / 2));
            canvas.drawText(game.player.getLive() == 0 ? "You're dead" : (game.player2.getLive() == 0 ? "Opponent is dead" : ""),
                    x, mSettings.CurrentYRes / 2 - 50, textPaint);
        }
    }
}
