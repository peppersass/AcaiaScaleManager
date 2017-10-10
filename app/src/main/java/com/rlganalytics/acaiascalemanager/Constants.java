package com.rlganalytics.acaiascalemanager;

/**
 * Copyright (c) 2017 RLG Analytics, LLC
 * All Rights Reserved.
 *
 * Constants class
 *
 * <p>Constants for AcaiaScaleManager. Defines all hard-coded values for the app. Most are static, some are initialized at startup</p>
 *
 */

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Constants {

    // scale constants

    public static final String SCALE_SERVICE_UUID = "00001820-0000-1000-8000-00805f9b34fb";
    public static final String SCALE_CHARACTERISTIC_UUID = "00002a80-0000-1000-8000-00805f9b34fb";

    public static final long SCAN_PERIOD = 10000;

    public static final byte HEADER1 = (byte) 0xef;
    public static final byte HEADER2 = (byte) 0xdd;

    public static final int STATE_HEADER = 0;
    public static final int STATE_DATA = 1;

    public static final int MSG_SYSTEM = 0;
    public static final int MSG_TARE = 4;
    public static final int MSG_INFO = 7;
    public static final int MSG_STATUS = 8;
    public static final int MSG_IDENTIFY = 11;
    public static final int MSG_EVENT = 12;
    public static final int MSG_TIMER = 13;

    public static final int EVENT_WEIGHT = 5;
    public static final int EVENT_BATTERY = 6;
    public static final int EVENT_TIMER = 7;
    public static final int EVENT_KEY = 8;
    public static final int EVENT_ACK = 11;


    public static final int EVENT_WEIGHT_LEN = 6;
    public static final int EVENT_BATTERY_LEN = 1;
    public static final int EVENT_TIMER_LEN = 3;
    public static final int EVENT_KEY_LEN = 1;
    public static final int EVENT_ACK_LEN = 2;


    public final static String ACTION_CONNECTION_STATE_CONNECTED = "ACTION_CONNECTION_STATE_CONNECTED";
    public final static String ACTION_CONNECTION_STATE_DISCONNECTED = "ACTION_CONNECTION_STATE_DISCONNECTED";
    public final static String ACTION_CONNECTION_STATE_DISCONNECTING = "ACTION_CONNECTION_STATE_DISCONNECTING";
    public final static String ACTION_CONNECTION_STATE_CONNECTING = "ACTION_CONNECTION_STATE_CONNECTING";

    public final static String ACTION_SERVICES_DISCOVERED = "ACTION_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String ACTION_DEVICE_FOUND = "ACTION_DEVICE_FOUND";

    public final static String ACTION_BLE_NOT_SUPPORTED = "ACTION_BLE_NOT_SUPPORTED";

    public final static String EXTRA_CONNECTION_STATE = "EXTRA_CONNECTION_STATE";
    public final static String EXTRA_DEVICE = "EXTRA_DEVICE";
    public final static String EXTRA_RSSI = "EXTRA_RSSI";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UNIT = "UNIT";
    public final static String EXTRA_DATA_TYPE = "DATA_TYPE";

    //Connection State
    public final static int CONNECTION_STATE_DISCONNECTED = 0;
    public final static int CONNECTION_STATE_DISCONNECTING = 1;
    public final static int CONNECTION_STATE_CONNECTED = 2;
    public final static int CONNECTION_STATE_CONNECTING = 3;

    public final static int AUTOOFF_TIME_5_MIN = 0;
    public final static int AUTOOFF_TIME_10_MIN = 1;
    public final static int AUTOOFF_TIME_15_MIN = 2;
    public final static int AUTOOFF_TIME_30_MIN = 3;

    public final static int DATA_TYPE_WEIGHT = 0;
    public final static int DATA_TYPE_BATTERY = 1;
    public final static int DATA_TYPE_AUTO_OFF_TIME = 2;
    public final static int DATA_TYPE_BEEP = 3;
    public final static int DATA_TYPE_KEY_DISABLED_ELAPSED_TIME = 4;
    public final static int DATA_TYPE_TIMER = 5;
    public final static int DATA_TYPE_SOUND = 6;

}