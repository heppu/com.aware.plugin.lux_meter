package com.aware.plugin.lux_meter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;


public class LuxAlarm extends BroadcastReceiver {    
	
	@Override
    public void onReceive(Context context, Intent intent) {
        Aware.setSetting(context, Aware_Preferences.STATUS_LIGHT, true);
        Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
        context.sendBroadcast(apply);
        Plugin.lockOff(context);
    }

	public void SetAlarm(Context context, int interval) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, LuxAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (interval*60*1000), pi);
	}

	public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, LuxAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}