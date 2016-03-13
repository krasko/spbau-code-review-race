package ru.spbau.anastasia.race;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.concurrent.FutureTask;

public class DeviseChooser extends BaseActivity {

    private static final int LENGTH_OF_USELESS_END_OF_STRING = 17;

    private BluetoothService btService;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 2;
    private static final int GAME_CONNECTION = 3;

    private ArrayAdapter<String> ArrayOfDevisesCapableToConnecting;

    private int devicesFound;

    private Intent btServiceIntent;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            try {
                btService.initBtAdapter();
            } catch (BluetoothService.BtUnavailableException e) {
                Toast.makeText(DeviseChooser.this, R.string.bluetooth_absent, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            btService.setOnConnected(new BluetoothService.OnConnected() {
                @Override
                public FutureTask success() {
                    FutureTask task = new FutureTask<>(new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(new Intent(DeviseChooser.this, GameConnection.class), GAME_CONNECTION);
                        }
                    }, null);
                    runOnUiThread(task);
                    return task;
                }
            });
            if (!btService.getBluetoothAdapter().isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
            } else {
                initBt();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    private View.OnClickListener onEnableDiscoverable = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
                    REQUEST_ENABLE_DISCOVERABLE
            );
        }
    };

    private View.OnClickListener onScanForDevices = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayOfDevisesCapableToConnecting.clear();
            btService.getBluetoothAdapter().startDiscovery();
            devicesFound = 0;
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            btService.getBluetoothAdapter().cancelDiscovery();

            String str = ((TextView) view).getText().toString();
            String address = str.substring(str.length() - LENGTH_OF_USELESS_END_OF_STRING);

            makeToast(address);

            btService.startConnectThread(address);
            ArrayOfDevisesCapableToConnecting.clear();
            initBt();
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ArrayOfDevisesCapableToConnecting.add(device.getName() + "\n" + device.getAddress());
                ++devicesFound;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                makeToast(Integer.toString(devicesFound) +
                        " " + getString(R.string.n_devices_found_suffix));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Button enableDiscoverable = (Button) findViewById(R.id.enable_discoverable);
        enableDiscoverable.setOnClickListener(onEnableDiscoverable);

        Button scanForDevices = (Button) findViewById(R.id.scan_for_devices);
        scanForDevices.setOnClickListener(onScanForDevices);

        ListView deviceList = (ListView) findViewById(R.id.device_list);
        ArrayOfDevisesCapableToConnecting = new ArrayAdapter<>(this, R.layout.device);
        deviceList.setAdapter(ArrayOfDevisesCapableToConnecting);
        deviceList.setOnItemClickListener(onItemClickListener);

        btServiceIntent = new Intent(this, BluetoothService.class);

        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                initBt();
            }
        } else if (requestCode == GAME_CONNECTION) {
            btService.write(GameConnection.STOP_MESSAGE.getBytes());
            btService.closeConnection();
            btService.startAcceptThread();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btService.getBluetoothAdapter() != null) {
            btService.getBluetoothAdapter().cancelDiscovery();
        }
        unregisterReceiver(broadcastReceiver);
        unbindService(connection);
        stopService(btServiceIntent);
    }

    private String getNameAndAddressFromDevice(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress();
    }

    private void initBt() {
        Set<BluetoothDevice> paired = btService.getBluetoothAdapter().getBondedDevices();
        for (BluetoothDevice bluetoothDevice : paired) {
            ArrayOfDevisesCapableToConnecting.add(getNameAndAddressFromDevice(bluetoothDevice));
        }
        btService.startAcceptThread();
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
