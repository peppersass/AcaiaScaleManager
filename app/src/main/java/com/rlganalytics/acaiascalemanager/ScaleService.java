package com.rlganalytics.acaiascalemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *  Created by Dick Green on 10/9/2017. Based on scale support code by Nicholas Pouvesle.
 */

@TargetApi(21)
public class ScaleService extends Service {

    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mServiceDiscoverDone = false;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = Constants.CONNECTION_STATE_DISCONNECTED;
    private BluetoothGatt mGatt;
    private final String TAG = ScaleService.class.getSimpleName();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Handler mHandler;
    private long lastHeartbeat;
    private Scale scale = null;

    private String currentWeight = "";
    private String currentBattery = "";

    public boolean initialize(Context mCtx, Scale myScale) {

        mHandler = new Handler();

        context = mCtx;
        scale = myScale;

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i("ScaleService", "BLE Not Supported");
            broadcastUpdate(Constants.ACTION_BLE_NOT_SUPPORTED);
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mServiceDiscoverDone = false;

        if (mBluetoothGatt != null) {
            mConnectionState = Constants.CONNECTION_STATE_DISCONNECTED;
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;

        // connect to hardcoded device address to speed startup
//        BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice("00:1C:97:12:69:44");
//        connectToDevice(mBluetoothDevice);

        // comment two above lines and uncomment this one to scan for scale
        scanLeDevice(true);

        return true;
    }

    public class LocalBinder extends Binder {
        public ScaleService getService() {
            return ScaleService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        // TODO Auto-generated method stub
        //return mBinder;
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public void scanLeDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        //mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, Constants.SCAN_PERIOD);

            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                //mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                //mLEScanner.stopScan(mScanCallback);
            }
        }
    }

//    private ScanCallback mScanCallback = new ScanCallback() {
//
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//
//            Log.i("callbackType", String.valueOf(callbackType));
//            Log.i("result", result.toString());
//            BluetoothDevice btDevice = result.getDevice();
//            connectToDevice(btDevice);
//        }
//
//
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//
//            for (ScanResult sr : results) {
//                Log.i("ScanResult - Results", sr.toString());
//            }
//        }
//
//
//        @Override
//        public void onScanFailed(int errorCode) {
//
//            Log.e("Scan Failed", "Error Code: " + errorCode);
//        }
//    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    String name = device.getName();
                    if (name.equals("PROCHBT001")) {
                        broadcastUpdate(Constants.ACTION_DEVICE_FOUND);
                        // connectToDevice(device);
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("onLeScan", device.toString());
                                connectToDevice(device);
                            }
                        });
                    }
                }
            };

    public void connectToDevice(BluetoothDevice device) {

        if (mGatt == null) {
            mGatt = device.connectGatt(context, false, gattCallback);
            // will stop after first device detection
            scanLeDevice(false);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    broadcastUpdate(Constants.ACTION_CONNECTION_STATE_CONNECTED);
                    gatt.discoverServices();
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    broadcastUpdate(Constants.ACTION_CONNECTION_STATE_DISCONNECTED);
                    break;

                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            BluetoothGattService service = gatt.getService(UUID.fromString(Constants.SCALE_SERVICE_UUID));
            Log.i("onServicesDiscovered", service.toString());
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(Constants.SCALE_CHARACTERISTIC_UUID));
            gatt.setCharacteristicNotification(characteristic, true);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptors().get(0);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

            // Run heartbeat every 3 seconds to keep connection alive

            final Runnable beeper = new Runnable() {
                public void run() { sendHeartbeat(); }
            };
            final ScheduledFuture<?> beeperHandle =
                    scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);

            //scheduler.scheduleAtFixedRate(scale::sendHeartbeat, 2, 3, TimeUnit.SECONDS);

            broadcastUpdate(Constants.ACTION_SERVICES_DISCOVERED);
        }

        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // Log.i(TAG, "data in");
        scale.processData(characteristic.getValue());
        String weight  = String.format("%.1f", scale.getWeight()) + "g";
        String battery = String.valueOf((scale.getBattery())) + "%";
        if (weight != currentWeight){
            intent.putExtra(Constants.EXTRA_DATA, weight);
            intent.putExtra(Constants.EXTRA_DATA_TYPE, Constants.DATA_TYPE_WEIGHT);
            sendBroadcast(intent);
            currentWeight = weight;
        }
        if (weight != currentBattery) {
            intent.putExtra(Constants.EXTRA_DATA, battery);
            intent.putExtra(Constants.EXTRA_DATA_TYPE, Constants.DATA_TYPE_BATTERY);
            sendBroadcast(intent);
            currentBattery = battery;
        }
    }

    public void SendMessage(int type, byte[] payload) {
        int cksum1 = 0;
        int cksum2 = 0;
        byte[] bytes = new byte[payload.length + 5];
        bytes[0] = Constants.HEADER1;
        bytes[1] = Constants.HEADER2;
        bytes[2] = (byte) type;

        for (int i = 0; i < payload.length; i++) {
            bytes[i + 3] = payload[i];
            if (i % 2 == 0) {
                cksum1 = (cksum1 + payload[i]) & 0xFF;
            } else {
                cksum2 = (cksum2 + payload[i]) & 0xFF;
            }
        }

        bytes[payload.length + 3] = (byte) cksum1;
        bytes[payload.length + 4] = (byte) cksum2;

        BluetoothGattCharacteristic characteristic = mGatt.getService(UUID.fromString(Constants.SCALE_SERVICE_UUID)).getCharacteristic(UUID.fromString(Constants.SCALE_CHARACTERISTIC_UUID));
        characteristic.setValue(bytes);
        mGatt.writeCharacteristic(characteristic);
    }

    public void sendHeartbeat() {
        long now = System.currentTimeMillis();
        if (lastHeartbeat + 3000 > now) {
            return;
        }
        byte[] payload = {0x02,0x00};
        SendMessage(Constants.MSG_SYSTEM, payload);
        lastHeartbeat = now;
    }

}