
package com.baked.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.R;

public class StatusBarClock extends BAKEDPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_ENABLE = "clock_style";
    private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
    private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";
    private static final String PREF_CLOCK_COLOR = "clock_color";

    ListPreference mClockStyle;
    ListPreference mClockAmPmstyle;
    ListPreference mClockWeekday;
    ColorPickerPreference mClockColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_statusbar_clock);

        mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
        mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
        mClockWeekday = (ListPreference) findPreference(PREF_CLOCK_WEEKDAY);
        mClockColor = (ColorPickerPreference) findPreference(PREF_CLOCK_COLOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        updateSummaries();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mClockAmPmstyle) {
            int index = mClockAmPmstyle.findIndexOfValue((String) newValue);
            preference.setSummary(mClockAmPmstyle.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);
            return true;
        } else if (preference == mClockStyle) {
            int index = mClockStyle.findIndexOfValue((String) newValue);
            preference.setSummary(mClockStyle.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CLOCK_STYLE, val);
            return true;
        } else if (preference == mClockWeekday) {
            int index = mClockWeekday.findIndexOfValue((String) newValue);
            preference.setSummary(mClockWeekday.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
            return true;
        } else if (preference == mClockColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockAmPmstyle.setOnPreferenceChangeListener(this);
        mClockWeekday.setOnPreferenceChangeListener(this);
        mClockColor.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mClockStyle.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_CLOCK_STYLE, 1)));
        mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 2)));
        mClockWeekday.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_CLOCK_WEEKDAY, 0)));
    }

    private void updateSummaries() {
        mClockStyle.setSummary(mClockStyle.getEntry());
        mClockAmPmstyle.setSummary(mClockAmPmstyle.getEntry());
        mClockWeekday.setSummary(mClockWeekday.getEntry());
        mClockColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_CLOCK_COLOR, 0xFF33B5E5)));
    }
}
