package com.rlganalytics.acaiascalemanager;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

@TargetApi(21)
public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

    private Scale scale = null;

    private TextView weightView;
    private TextView batteryView;

    private final static String TAG = "AcaiaScaleManager"+MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        weightView = (TextView) findViewById(R.id.weightView);
        batteryView = (TextView) findViewById(R.id.batteryView);

        mHandler = new Handler();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        scale = new Scale();
        scale.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_CONNECTION_STATE_CONNECTED);
        intentFilter.addAction(Constants.ACTION_CONNECTION_STATE_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_CONNECTION_STATE_DISCONNECTING);
        intentFilter.addAction(Constants.ACTION_CONNECTION_STATE_CONNECTING);
        intentFilter.addAction(Constants.ACTION_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_DEVICE_FOUND);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Constants.ACTION_CONNECTION_STATE_CONNECTED:
                    break;
                case Constants.ACTION_CONNECTION_STATE_DISCONNECTED:
                    updateTextView(weightView, "N/C");
                    updateTextView(batteryView, "N/C");
                    break;
                case Constants.ACTION_DEVICE_FOUND:
                    break;
                case Constants.ACTION_DATA_AVAILABLE:
                    int type = intent.getIntExtra(Constants.EXTRA_DATA_TYPE, -1);
                    if (type == Constants.DATA_TYPE_WEIGHT) {
                        updateTextView(weightView, intent.getStringExtra(Constants.EXTRA_DATA));
                    } else if (type == Constants.DATA_TYPE_BATTERY) {
                        updateTextView(batteryView, intent.getStringExtra(Constants.EXTRA_DATA));
                    }
                    break;
                case Constants.ACTION_BLE_NOT_SUPPORTED:
                    updateTextView(weightView, "BLE NOT SUPPORTED!");
                    updateTextView(batteryView, "BLE NOT SUPPORTED!");
                    break;
            }
        }
    };

    void updateTextView(TextView t, String text) {
        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This gets executed on the UI thread so it can safely modify Views
                t.setText(text);
            }
        });
    }
}