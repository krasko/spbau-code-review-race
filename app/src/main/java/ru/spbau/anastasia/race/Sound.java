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

/**
 * The Sound class provides music and sounds in the game.
 */
public class Sound {

    /**
     * Boolean means is the sound turn off (True) or turn on (False), player can choose it in the
     * GameMenu activity. Some sounds can be different in different themes. The number of the
     * current theme is in the integer.
     */
    public boolean isStopped;
    public int theme;

    /**
     * Integers describes three types of sound (not music) that player can hear during the race.
     */
    public static final int CRASH = 1;
    public static final int LOSE = 2;
    public static final int JUMP = 3;

    /**
     * The background music repeats every period of this time interval. 
     */
    private static final int MUSIC_TIME = 90000;

    private int soundLose, soundCrash, soundJump, soundTheme;
    private SoundPool soundPool;
    private AssetManager assetManager;
    private int streamID;

    class SceneTask extends TimerTask {
        @Override
        public void run() {
            if (isStopped) {
                return;
            }
            playSound(soundTheme);
        }
    }

    public Sound(AssetManager asset, int theme, int menu) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // For devices to Android 5
            createOldSoundPool();
        } else {
            // For new devices
            createNewSoundPool();
        }

        if (menu == GameMenu.MENU_ACTIVITY) {
            SceneTask task = new SceneTask();
            Timer timer = new Timer();
            timer.schedule(task, 0, MUSIC_TIME);
            this.theme = theme;
        }

        isStopped = false;
        assetManager = asset;
        soundLose = loadSound("lose.mp3");
        soundCrash = loadSound("crash.mp3");

        if (theme == GameMenu.NOT_IS_CHECKED) {
            soundJump = loadSound("jump.mp3");
        } else {
            soundJump = loadSound("jump_in_snow.mp3");
        }

        soundTheme = loadSound("race.mp3");
    }

    @SuppressWarnings("deprecation")
    public void createOldSoundPool() {
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    }

    public void play(int sound) {
        if (isStopped) {
            return;
        }
        switch (sound) {
            case LOSE:
                playSound(soundLose);
                break;
            case JUMP:
                playSound(soundJump);
                break;
            case CRASH:
                playSound(soundCrash);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    private int playSound(int sound) {
        if (sound > 0) {
            streamID = soundPool.play(sound, 1, 1, 1, 0, 1);
        }
        return streamID;
    }

    private int loadSound(String fileName) {
        AssetFileDescriptor afd;
        try {
            afd = assetManager.openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return soundPool.load(afd, 1);
    }
}
