package com.rlganalytics.acaiascalemanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

/**
 * Scale class by Nicholas Pouvesle, modified by Dick Green 10/9/2017
 *
 */

public class Scale {

    private int state;
    private byte msgType;
    private int battery;
    private boolean notificationInfoSent;
    private boolean ready;
    private float weight;
    private boolean weightHasChanged;
    private int minutes;
    private int seconds;
    private int mseconds;
    private Context mCtx = null;
    private ScaleService mScaleService = null;
    private Scale myObject;

    private final static String TAG = Scale.class.getSimpleName();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mScaleService = ((ScaleService.LocalBinder) service).getService();
            if (!mScaleService.initialize(mCtx, myObject)) {
                Log.i(TAG, "ScaleService initialize failed!");
                return;
            }

            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mScaleService = null;
        }
    };

    public boolean initialize (Context context) {
        if (mCtx != null) {
            //Log.d(TAG, "Please call release() of Scale class first");
            //return false;

            Log.d(TAG, "context is not null, releasing...");
            release();
            return false;
        }
        mCtx = context;
        myObject = this;

        Intent scaleServiceIntent = new Intent(mCtx, ScaleService.class);
        mCtx.bindService(scaleServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        return true;
    }

    public boolean release() {
        Log.d(TAG, "Release...");
        mCtx.unbindService(mServiceConnection);
        mCtx = null;
        return true;
    }

//
//
//    public void scanDevice(boolean enable) {
//        mScaleService.scanLeDevice(true);
//    }

    public void sendEvent(byte[] payload) {
        byte[] bytes = new byte[payload.length + 1];
        bytes[0] = (byte)(payload.length + 1);

        for (int i = 0; i < payload.length; i++) {
            bytes[i+1] = (byte)(payload[i] & 0xff);
        }

        mScaleService.SendMessage(Constants.MSG_EVENT, bytes);
    }

    public void sendTare() {
        byte[] payload = {0x00};
        mScaleService.SendMessage(Constants.MSG_TARE, payload);
    }


    public void sendTimerCommand(int command) {
        byte[] payload = {0x00, (byte)command};
        mScaleService.SendMessage(Constants.MSG_TIMER, payload);
    }

    public void sendId() {
        byte[] payload = {0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d,0x2d};
        mScaleService.SendMessage(Constants.MSG_IDENTIFY, payload);
    }

    public void sendNotificationRequest() {
        byte[] payload = {
                0,  // weight
                1,  // weight argument
                1,  // battery
                2,  // battery argument
                2,  // timer
                5,  // timer argument
                3,  // key
                4   // setting
        };

        sendEvent(payload);
        ready = true;
        notificationInfoSent = true;
    }


    public void dump(String msg, byte[] payload) {
        StringBuilder sb = new StringBuilder();
        for (byte b : payload) {
            sb.append(String.format("%02X ", b));
        }
        Log.e("scaleDump", msg + sb.toString());
    }

    public int parseWeightEvent(byte[] payload) {
        if (payload.length < Constants.EVENT_WEIGHT_LEN) {
            dump("Invalid weight event: ", payload);
            return -1;
        }

        float value = (((payload[1] & 0xff) << 8) + (payload[0] & 0xff));
        int unit = payload[4] & 0xFF;

        if (unit == 1) {
            value /= 10;
        }
        else if (unit == 2) {
            value /= 100;
        }
        else if (unit == 3) {
            value /= 1000;
        }
        else if (unit == 4) {
            value /= 10000;
        }

        if ((payload[5] & 0x02) == 0x02) {
            value *= -1;
        }

        weight = value;
        weightHasChanged = true;

        return Constants.EVENT_WEIGHT_LEN;
    }


    public int parseAckEvent(byte[] payload) {
        if (payload.length < Constants.EVENT_ACK_LEN) {
            dump("Invalid ack event: ", payload);
            return -1;
        }

        // ignore ack

        return Constants.EVENT_ACK_LEN;
    }

    public int parseKeyEvent(byte[] payload) {
        if (payload.length < Constants.EVENT_KEY_LEN) {
            dump("Invalid ack event length: ", payload);
            return -1;
        }

        // ignore key event

        return Constants.EVENT_KEY_LEN;
    }

    public int parseBatteryEvent(byte[] payload) {
        if (payload.length < Constants.EVENT_BATTERY_LEN) {
            dump("Invalid battery event length: ", payload);
            return -1;
        }

        battery = payload[0];

        return Constants.EVENT_BATTERY_LEN;
    }


    public int parseTimerEvent(byte[] payload) {
        if (payload.length < Constants.EVENT_TIMER_LEN) {
            dump("Invalid timer event length: ", payload);
            return -1;
        }

        minutes = payload[0];
        seconds = payload[1];
        mseconds = payload[2];

        return Constants.EVENT_TIMER_LEN;
    }


    // returns last position in payload
    public int parseScaleEvent(byte[] payload) {
        int event = payload[0];
        int val;
        byte[] bytes = Arrays.copyOfRange(payload, 1, payload.length);

        switch(event) {
            case Constants.EVENT_WEIGHT:
                val = parseWeightEvent(bytes);
                break;
            case Constants.EVENT_BATTERY:
                val = parseBatteryEvent(bytes);
                break;
            case Constants.EVENT_TIMER:
                val = parseTimerEvent(bytes);
                break;
            case Constants.EVENT_ACK:
                val = parseAckEvent(bytes);
                break;
            case Constants.EVENT_KEY:
                val = parseKeyEvent(bytes);
                break;
            default:
                //               dump("Unknown event: ", payload);
                return -1;
        }
        if (val < 0) {
            return -1;
        }
        return val + 1;
    }


    public int parseScaleEvents(byte[] payload) {
        int lastPos = 0;
        while (lastPos < payload.length) {
            byte[] bytes = Arrays.copyOfRange(payload, lastPos, payload.length);

            int pos = parseScaleEvent(bytes);
            if (pos < 0) {
                return -1;
            }

            lastPos += pos;
        }

        return 0;
    }


    public int parseInfo(byte[] payload) {
        battery = payload[4];
        // TODO parse other infos

        return 0;
    }

    public int parseScaleData(byte[] data) {
        int ret = 0;

        switch(msgType) {
            case Constants.MSG_INFO:
                ret = parseInfo(data);
                sendId();
                break;

            case Constants.MSG_STATUS:
                if (!notificationInfoSent) {
                    sendNotificationRequest();
                }
                break;

            case Constants.MSG_EVENT:
                ret = parseScaleEvents(data);
                break;

            default:
                break;
        }

        return ret;
    }


    public void processData(byte[] data) {

        if (state == Constants.STATE_HEADER) {
            if (data.length != 3) {
                Log.e("Invalid header length", data.toString());
                return;
            }

            if (data[0] != Constants.HEADER1 || data[1] != Constants.HEADER2) {
                Log.e("Invalid header: ", data.toString());
                return;
            }

            state = Constants.STATE_DATA;
            msgType = data[2];
        } else {

            int len;
            int offset = 0;

            if (msgType == Constants.MSG_STATUS || msgType == Constants.MSG_EVENT || msgType == Constants.MSG_INFO) {
                len = data[0];
                if (len == 0) {
                    len = 1;
                }
                offset = 1;
            } else {
                switch (msgType) {
                    case 0:
                        len = 2;
                        break;

                    default:
                        len = 0;
                }
            }

            if (data.length < len + 2) {
                Log.e("Invalid data length", data.toString());
            }

            parseScaleData(Arrays.copyOfRange(data, offset, offset + len - 1));
            state = Constants.STATE_HEADER;
        }
    }

    public float getWeight() {
        return weight;
    }

    public int getBattery() {
        return battery;
    }

}

