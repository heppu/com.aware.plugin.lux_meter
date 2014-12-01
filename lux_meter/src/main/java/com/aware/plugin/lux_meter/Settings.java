package com.aware.plugin.lux_meter;

import com.aware.Aware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_LUX_METER = "status_plugin_lux_meter";

    public static final String MODE_PLUGIN_LUX_METER = "mode_plugin_lux_meter";

    public static final String FREQUENCY_PLUGIN_LUX_METER = "frequency_plugin_lux_meter";

    public static final String SAMPLES_PLUGIN_LUX_METER = "samples_plugin_lux_meter";

    public static final String THRESHOLD_PLUGIN_LUX_METER = "threshold_plugin_lux_meter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        syncSettings();
    }

    private void syncSettings() {
        CheckBoxPreference active = (CheckBoxPreference) findPreference(STATUS_PLUGIN_LUX_METER);
        active.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_LUX_METER).equals("true"));

        CheckBoxPreference mode = (CheckBoxPreference) findPreference(MODE_PLUGIN_LUX_METER);
        boolean mode_sate = Aware.getSetting(getApplicationContext(), MODE_PLUGIN_LUX_METER).equals("true");
        mode.setChecked(mode_sate);

        EditTextPreference frequency = (EditTextPreference) findPreference(FREQUENCY_PLUGIN_LUX_METER);
        frequency.setEnabled(!mode_sate);
        if( Aware.getSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER, 5);
        }
        frequency.setSummary(Aware.getSetting(getApplicationContext(), FREQUENCY_PLUGIN_LUX_METER) + " minutes");

        EditTextPreference sample = (EditTextPreference) findPreference(SAMPLES_PLUGIN_LUX_METER);
        sample.setEnabled(!mode_sate);
        if( Aware.getSetting(getApplicationContext(), SAMPLES_PLUGIN_LUX_METER).length() == 0 ) {
            Aware.setSetting(getApplicationContext(), SAMPLES_PLUGIN_LUX_METER, 30);
        }
        sample.setSummary(Aware.getSetting(getApplicationContext(), SAMPLES_PLUGIN_LUX_METER) + " samples");

        EditTextPreference threshold = (EditTextPreference) findPreference(THRESHOLD_PLUGIN_LUX_METER);
        threshold.setEnabled(!mode_sate);
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

        if( preference.getKey().equals(MODE_PLUGIN_LUX_METER)) {
            boolean mode_sate = sharedPreferences.getBoolean(key, false);
            EditTextPreference frequency = (EditTextPreference) findPreference(FREQUENCY_PLUGIN_LUX_METER);
            frequency.setEnabled(!mode_sate);
            EditTextPreference threshold = (EditTextPreference) findPreference(THRESHOLD_PLUGIN_LUX_METER);
            threshold.setEnabled(!mode_sate);
            EditTextPreference sample = (EditTextPreference) findPreference(SAMPLES_PLUGIN_LUX_METER);
            sample.setEnabled(!mode_sate);
            Aware.setSetting(getApplicationContext(), key, Boolean.toString(mode_sate));
        }

        if( preference.getKey().equals(FREQUENCY_PLUGIN_LUX_METER)) {
            preference.setSummary(sharedPreferences.getString(key, "5") + " minutes");
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "5"));
        }

        if( preference.getKey().equals(SAMPLES_PLUGIN_LUX_METER)) {
            preference.setSummary(sharedPreferences.getString(key, "30") + " samples");
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "30"));
        }

        if( preference.getKey().equals(THRESHOLD_PLUGIN_LUX_METER)) {
            preference.setSummary(sharedPreferences.getString(key, "100") + " lux");
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "100"));
        }

        Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(apply);
    }
}

