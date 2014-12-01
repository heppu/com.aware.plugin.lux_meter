package com.aware.plugin.lux_meter;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Light;
import com.aware.ui.Stream_UI;
import com.aware.Aware_Preferences;
import com.aware.plugin.lux_meter.Provider.LuxMeter_Data;
import com.aware.providers.Light_Provider.Light_Data;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_LUX_METER = "ACTION_AWARE_PLUGIN_LUX_METER";

    public static final String EXTRA_OVER_THRESHOLD = "over_threshold";

    public static final String EXTRA_AVG_LUX = "avg_lux";

    private static boolean over_threashold;

    public static int temp_interval = 0;
    public static boolean temp_mode = false;

    public LuxAlarm alarm = new LuxAlarm();

    public static ContextProducer context_producer;

    private static int avg = 0;

    private static double counter = 0;
    private static double avg_val = 0;
    private static double current = 0;
    private static boolean lock = false;

    private static StreamStateReceiver streamReceiver = new StreamStateReceiver();

    private LightReceiver lightReceiver = new LightReceiver();

    public static class StreamStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean mode =  Boolean.parseBoolean(Aware.getSetting(context, Settings.MODE_PLUGIN_LUX_METER));
            if(mode) {
                if (action.equals(Stream_UI.ACTION_AWARE_STREAM_CLOSED)) {
                    Aware.setSetting(context, Aware_Preferences.STATUS_LIGHT, false);
                    Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
                    context.sendBroadcast(apply);

                } else if (action.equals(Stream_UI.ACTION_AWARE_STREAM_OPEN)) {
                    Aware.setSetting(context, Aware_Preferences.STATUS_LIGHT, true);
                    Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
                    context.sendBroadcast(apply);
                }
            }
        }
    }

    public class LightReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int interval_min =  Integer.parseInt(Aware.getSetting(context, Settings.FREQUENCY_PLUGIN_LUX_METER));
            int samples =  Integer.parseInt(Aware.getSetting(context, Settings.SAMPLES_PLUGIN_LUX_METER));
            boolean mode =  Boolean.parseBoolean(Aware.getSetting(context, Settings.MODE_PLUGIN_LUX_METER));

            if (interval_min > 0 && !lock && !mode) {

                if (counter < samples) {
                    ContentValues values = (ContentValues) intent.getExtras().get(Light.EXTRA_DATA);
                    current = Double.parseDouble(values.get(Light_Data.LIGHT_LUX).toString());
                    avg_val = avg_val + current;
                    counter = counter + 1;
                } else {
                    lock = true;
                    avg = (int)(avg_val/counter);
                    over_threashold = false;

                    if( Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.THRESHOLD_PLUGIN_LUX_METER)) < avg ) {
                        over_threashold = true;
                    }

                    Aware.setSetting(context, Aware_Preferences.STATUS_LIGHT, false);
                    Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
                    context.sendBroadcast(apply);

                    ContentValues data = new ContentValues();
                    data.put(LuxMeter_Data.TIMESTAMP, System.currentTimeMillis());
                    data.put(LuxMeter_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    data.put(LuxMeter_Data.UPDATE_FREQUENCY, Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_LUX_METER)));
                    data.put(LuxMeter_Data.LIGHT_THRESHOLD, Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.THRESHOLD_PLUGIN_LUX_METER)));
                    data.put(LuxMeter_Data.LUX_AVG, avg);
                    data.put(LuxMeter_Data.OVER_THRESHOLD, over_threashold);
                    getContentResolver().insert(LuxMeter_Data.CONTENT_URI, data);
                    context_producer.onContext();

                    alarm.SetAlarm(context, interval_min);

                    counter = 0;
                    avg_val = 0;
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.d("asd", "onCreate");
        super.onCreate();
        TAG = "AWARE::Lux Meter";

        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_LUX_METER, true);

        if( Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_LUX_METER).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_LUX_METER, 10);
        }

        if( Aware.getSetting(getApplicationContext(), Settings.MODE_PLUGIN_LUX_METER).length() == 0) {
            Aware.setSetting(getApplicationContext(), Settings.MODE_PLUGIN_LUX_METER, "false");
        }

        if( Aware.getSetting(getApplicationContext(), Settings.THRESHOLD_PLUGIN_LUX_METER).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), Settings.THRESHOLD_PLUGIN_LUX_METER, 100);
        }

        context_producer = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context_lux_meter = new Intent();
                context_lux_meter.setAction(ACTION_AWARE_PLUGIN_LUX_METER);
                context_lux_meter.putExtra(EXTRA_AVG_LUX, avg);
                context_lux_meter.putExtra(EXTRA_OVER_THRESHOLD, over_threashold);
                sendBroadcast(context_lux_meter);
            }
        };

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ LuxMeter_Data.CONTENT_URI };

        IntentFilter streamFilter = new IntentFilter();
        streamFilter.addAction(Stream_UI.ACTION_AWARE_STREAM_CLOSED);
        streamFilter.addAction(Stream_UI.ACTION_AWARE_STREAM_OPEN);
        registerReceiver(streamReceiver, streamFilter);

        IntentFilter lightFilter = new IntentFilter();
        lightFilter.addAction(Light.ACTION_AWARE_LIGHT);
        registerReceiver(lightReceiver, lightFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int interval_min =  Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_LUX_METER));
        boolean mode =  Boolean.parseBoolean(Aware.getSetting(getApplicationContext(), Settings.MODE_PLUGIN_LUX_METER));

        if (interval_min != temp_interval || mode != temp_mode) {
            if(interval_min >= 1 && !mode) {
                alarm.SetAlarm(Plugin.this, interval_min);
            } else {
                alarm.CancelAlarm(Plugin.this);
            }
            temp_interval = interval_min;
            temp_mode = mode;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        temp_interval = 0;
        alarm.CancelAlarm(Plugin.this);

        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_LUX_METER, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LIGHT, false);

        unregisterReceiver(streamReceiver);
        unregisterReceiver(lightReceiver);

        Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(apply);
    }

    protected static void lockOff(Context context) {
        lock = false;
    }
}
