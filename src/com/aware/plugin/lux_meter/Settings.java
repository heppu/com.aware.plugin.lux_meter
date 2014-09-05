package com.aware.plugin.lux_meter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;
import com.aware.plugin.lux_meter.R;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String STATUS_PLUGIN_LUX_METER = "status_plugin_lux_meter";
	
	public static final String FREQUENCY_PLUGIN_LUX_METER = "frequency_plugin_lux_meter";
	
	protected static final String THRESHOLD_PLUGIN_LUX_METER = "threshold_plugin_lux_meter";
	
	protected static final String TIME_WINDOW_PLUGIN_LUX_METER = "time_window_plugin_lux_meter";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.plugin_settings);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		syncSettings();
	}
	
	private void syncSettings() {
		CheckBoxPreference active = (CheckBoxPreference) findPreference(STATUS_PLUGIN_LUX_METER);
		active.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_LUX_METER).equals("true"));
		
		EditTextPreference frequency = (EditTextPreference) findPreference(FREQUENCY_PLUGIN_LUX_METER);
		if( Aware.getSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER, 5);
		}
		frequency.setSummary(Aware.getSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER) + " minutes");
		

		EditTextPreference time_window = (EditTextPreference) findPreference(TIME_WINDOW_PLUGIN_LUX_METER);
		if( Aware.getSetting(getApplicationContext(), TIME_WINDOW_PLUGIN_LUX_METER).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), TIME_WINDOW_PLUGIN_LUX_METER, 1);
		}
		time_window.setSummary(Aware.getSetting(getApplicationContext(), TIME_WINDOW_PLUGIN_LUX_METER) + " minutes");
		
		EditTextPreference threshold = (EditTextPreference) findPreference(THRESHOLD_PLUGIN_LUX_METER);
		if( Aware.getSetting(getApplicationContext(), THRESHOLD_PLUGIN_LUX_METER).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), THRESHOLD_PLUGIN_LUX_METER, 100);
		}
		threshold.setSummary(Aware.getSetting(getApplicationContext(), THRESHOLD_PLUGIN_LUX_METER) + " lux");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = (Preference) findPreference(key);
		
		if( preference.getKey().equals(STATUS_PLUGIN_LUX_METER)) {
			boolean is_active = sharedPreferences.getBoolean(key, false);
			Aware.setSetting(getApplicationContext(), key, is_active);
			if( is_active ) {
				Aware.startPlugin(getApplicationContext(), getPackageName());
			} else {
				Aware.stopPlugin(getApplicationContext(), getPackageName());
			}	
		}
		
		if( preference.getKey().equals(FREQUENCY_PLUGIN_LUX_METER)) {
			preference.setSummary(sharedPreferences.getString(key, "5") + " minutes");
			Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "5"));
		}
		
		if( preference.getKey().equals(TIME_WINDOW_PLUGIN_LUX_METER)) {
			preference.setSummary(sharedPreferences.getString(key, "1") + " minutes");
			Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "1"));
		}
		
		if( preference.getKey().equals(THRESHOLD_PLUGIN_LUX_METER)) {
			preference.setSummary(sharedPreferences.getString(key, "100") + " lux");
			Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "100"));
		}
		
		Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
		sendBroadcast(apply);
	}
}
