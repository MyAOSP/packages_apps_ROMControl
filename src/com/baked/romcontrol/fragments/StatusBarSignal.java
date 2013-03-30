
package com.baked.romcontrol.fragments;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;

public class StatusBarSignal extends BAKEDPreferenceFragment implements
        OnPreferenceChangeListener {

    ListPreference mDbmStyle;
    ListPreference mWifiStyle;
    ColorPickerPreference mColorPicker;
    ColorPickerPreference mWifiColorPicker;
    CheckBoxPreference mHideSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_statusbar_signal);

        mDbmStyle = (ListPreference) findPreference("signal_style");
        mColorPicker = (ColorPickerPreference) findPreference("signal_color");
        mWifiStyle = (ListPreference) findPreference("wifi_signal_style");
        mWifiColorPicker = (ColorPickerPreference) findPreference("wifi_signal_color");
        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        updateSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mHideSignal) {
            Settings.System.putInt(mContentResolver, Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDbmStyle) {
            int index = mDbmStyle.findIndexOfValue((String) newValue);
            mDbmStyle.setSummary(mDbmStyle.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.STATUSBAR_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(
                    String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mWifiStyle) {
            int index = mWifiStyle.findIndexOfValue((String) newValue);
            mWifiStyle.setSummary(mWifiStyle.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mWifiColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mDbmStyle.setOnPreferenceChangeListener(this);
        mColorPicker.setOnPreferenceChangeListener(this);
        mWifiStyle.setOnPreferenceChangeListener(this);
        mWifiColorPicker.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mDbmStyle.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_SIGNAL_TEXT, 0)));
        mWifiStyle.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT, 0)));
        mHideSignal.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_HIDE_SIGNAL_BARS, 0) != 0);
    }

    private void updateSummaries() {
        mDbmStyle.setSummary(mDbmStyle.getEntry());
        mColorPicker.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, 0xFF33B5E5)));
        mWifiStyle.setSummary(mWifiStyle.getEntry());
        mWifiColorPicker.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_WIFI_SIGNAL_TEXT_COLOR, 0xFF33B5E5)));
    }
}
