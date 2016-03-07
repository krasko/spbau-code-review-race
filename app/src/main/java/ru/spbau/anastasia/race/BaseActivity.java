package ru.spbau.anastasia.race;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BaseActivity extends Activity {

    protected int numOfTheme;
    protected boolean isSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            numOfTheme = extras.getInt("theme");
            isSound = extras.getBoolean("sound");
        }
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra("theme", numOfTheme);
        intent.putExtra("sound", isSound);
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra("theme", numOfTheme);
        intent.putExtra("sound", isSound);
        super.startActivityForResult(intent, requestCode);
    }
}
