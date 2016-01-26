package ru.spbau.anastasia.race;

import ru.spbau.anastasia.race.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.concurrent.FutureTask;

public class Choose extends Activity {

    private BluetoothService btService;

    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    private ArrayAdapter<String> arrayAdapter;

    private int devicesFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Button enableDiscoverable = (Button) findViewById(R.id.enable_discoverable);
        enableDiscoverable.setOnClickListener(onEnableDiscoverable);

        Button scanForDevices = (Button) findViewById(R.id.scan_for_devices);
        scanForDevices.setOnClickListener(onScanForDevices);

        ListView deviceList = (ListView) findViewById(R.id.device_list);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.device);
        deviceList.setAdapter(arrayAdapter);
        deviceList.setOnItemClickListener(onItemClickListener);

        bindService(new Intent(this, BluetoothService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.setOnConnected(new BluetoothService.OnConnected() {
                @Override
                public FutureTask success() {
                    FutureTask task = new FutureTask(new Runnable() {
                        @Override
                        public void run() {
                            Choose.this.setResult(RESULT_OK);
                            Choose.this.finish();
                        }
                    }, 1);
                    runOnUiThread(task);
                    return task;
                }
            });
            initBt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    private void initBt() {
        Set<BluetoothDevice> paired = btService.getBluetoothAdapter().getBondedDevices();
        for (BluetoothDevice device : paired) {
            arrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        btService.startAcceptThread();
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener onEnableDiscoverable = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
                    REQUEST_ENABLE_DISCOVERABLE);
        }
    };

    private View.OnClickListener onScanForDevices = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            arrayAdapter.clear();
            btService.getBluetoothAdapter().startDiscovery();
            devicesFound = 0;
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            btService.getBluetoothAdapter().cancelDiscovery();

            String str = ((TextView) view).getText().toString();
            String address = str.substring(str.length() - 17);

            makeToast(address);

            btService.startConnectThread(address);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                ++devicesFound;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                makeToast(Integer.toString(devicesFound) +
                        " " + getString(R.string.n_devices_found_suffix));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btService.getBluetoothAdapter() != null) {
            btService.getBluetoothAdapter().cancelDiscovery();
        }
        unregisterReceiver(broadcastReceiver);
        unbindService(serviceConnection);
    }
}
