package ru.spbau.anastasia.race;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Sound {

    public static final int MUSIC_TIME = 90000;
    public static final int CRASH = 1;
    public static final int LOSE = 2;
    public static final int JUMP = 3;

    private int mLose, mCrash, mJump, mTheme;
    private SoundPool mSoundPool;
    private AssetManager mAssetManager;
    private int mStreamID;

    public boolean isStopped;
    public int theme;

    class SceneTask extends TimerTask {
        @Override
        public void run() {
            if (isStopped) {
                return;
            }
            playSound(mTheme);
        }
    }

    public Sound(AssetManager asset, int theme_, int menu) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Для устройств до Android 5
            createOldSoundPool();
        } else {
            // Для новых устройств
            createNewSoundPool();
        }

        if (menu == GameMenu.MENU_ACTIVITY) {
            SceneTask task = new SceneTask();
            Timer timer = new Timer();
            timer.schedule(task, 0, MUSIC_TIME);
            theme = theme_;
        }

        isStopped = false;
        mAssetManager = asset;
        mLose = loadSound("lose.mp3");
        mCrash = loadSound("crash.mp3");

        if (theme == GameMenu.NOT_IS_CHECKED) {
            mJump = loadSound("jump.mp3");
        } else {
            mJump = loadSound("jump_in_snow.mp3");
        }

        mTheme = loadSound("race.mp3");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    public void createOldSoundPool() {
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    }

    public void play(int sound) {
        if (isStopped) {
            return;
        }
        switch (sound) {
            case LOSE:
                playSound(mLose);
                break;
            case JUMP:
               playSound(mJump);
                break;
            case CRASH:
                playSound(mCrash);
                break;
        }
    }

    private int playSound(int sound) {
        if (sound > 0) {
            mStreamID = mSoundPool.play(sound, 1, 1, 1, 0, 1);
        }
        return mStreamID;
    }

    private int loadSound(String fileName) {
        AssetFileDescriptor afd;
        try {
            afd = mAssetManager.openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return mSoundPool.load(afd, 1);
    }
}
