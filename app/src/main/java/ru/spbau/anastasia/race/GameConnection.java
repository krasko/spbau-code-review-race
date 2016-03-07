package ru.spbau.anastasia.race;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.Arrays;

public class GameConnection extends BaseActivity {

    public static final String STOP_MESSAGE = "stop";

    private static final int RECEIVED_BLOCK_LENGTH = 4;
    private static final int REQUEST_GAME = 1;
    private static final int HANDLER_MESSAGE_GET = 1;

    private static final String START_MESSAGE = "start";

    private static final String TAG = "GameConnection";

    private ArrayAdapter<String> arrayAdapter;
    private EditText editText;

    private BluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_game);

        ImageView backgroundImage = (ImageView) findViewById(R.id.ConnectionView);

        if (numOfTheme == GameMenu.IS_CHECKED) {
            backgroundImage.setImageResource(R.drawable.connection_game);
        } else {
            backgroundImage.setImageResource(R.drawable.connection_game2);
        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        ((ListView) findViewById(R.id.messages)).setAdapter(arrayAdapter);
        editText = (EditText) findViewById(R.id.edit_text);
        Button button = (Button) findViewById(R.id.send);
        button.setOnClickListener(onClickListener);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);
    }

    public void onClickButtonReconnection(View view) {
        finish();
    }

    public void onClickButtonStartPlay(View view) {
        if (btService != null && btService.isConnectionBegin()) {
            btService.write(START_MESSAGE.getBytes());
            startGame(true);
        }
    }

    public void onClickButtonBackTwoPlayerOption(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            byte[] bytes = Arrays.copyOf((byte[]) msg.obj, msg.arg1);
            Log.d(TAG, "handled : " + Arrays.toString(bytes) + "  - " + new String(bytes));
            if (bytes[0] == RoadForTwo.FINISH_ACTIVITY || bytes[0] == RoadForTwo.GAME_OVER) {
                return;
            }
            boolean stopFlag = true;
            for (int i = 0; i < RECEIVED_BLOCK_LENGTH; i++) {
                if (bytes[i] != STOP_MESSAGE.getBytes()[i]) {
                    stopFlag = false;
                }
            }
            if (stopFlag) {
                arrayAdapter.clear();
                finish();
                return;
            }
            boolean startFlag = true;
            for (int i = 0; i < 5; i++) { // TODO
                if (bytes[i] !=
                        START_MESSAGE.getBytes()[i]) {
                    startFlag = false;
                }
            }
            if (startFlag) {
                startGame(false);
                return;
            }

            arrayAdapter.add(btService.getBluetoothSocket().getRemoteDevice().getName() + ": " +
                    new String(bytes));
        }
    };

    private final BluetoothService.MessageReceiver gameConnectionMessageReceiver = new BluetoothService.MessageReceiver() {
        @Override
        public void process(int bytes, byte[] buffer) {
            handler.obtainMessage(HANDLER_MESSAGE_GET, bytes, -1, buffer).sendToTarget();
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @TargetApi(Build.VERSION_CODES.ECLAIR)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            btService.setMessageReceiver(gameConnectionMessageReceiver);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GAME) {
            btService.write(new byte[]{RoadForTwo.FINISH_ACTIVITY});
            arrayAdapter.clear();
            btService.setMessageReceiver(gameConnectionMessageReceiver);

        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btService.write(editText.getText().toString().getBytes());
            arrayAdapter.add("Me: " + editText.getText().toString());
            editText.getText().clear();
        }
    };

    public void onClickButtonBackRoadForTwo(View view) {
        finish();
    }

    protected void startGame(boolean isClient) {
        btService.setMessageReceiver(null);
        startActivityForResult(new Intent(this, RoadForTwo.class).putExtra("isServer", !isClient), REQUEST_GAME);
    }
}
