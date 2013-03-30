
package com.baked.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.preferences.ImageListPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarBattery extends BAKEDPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_BATT_ICON = "battery_icon_list";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";

    ImageListPreference mBatteryIcon;
    ListPreference mBatteryBar;
    ListPreference mBatteryBarStyle;
    ListPreference mBatteryBarThickness;
    CheckBoxPreference mBatteryBarChargingAnimation;
    ColorPickerPreference mBatteryBarColor;
    ColorPickerPreference mBatteryTextColor;
    ColorPickerPreference mBatteryChargeTextColor;
    ColorPickerPreference mCmCirleRingColor;
    ColorPickerPreference mCmCirleRingColorCharge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_statusbar_battery);

        mBatteryIcon = (ImageListPreference) findPreference(PREF_BATT_ICON);
        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarChargingAnimation = (CheckBoxPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        mBatteryChargeTextColor = (ColorPickerPreference)
                findPreference("battery_charge_text_only_color");
        mBatteryTextColor = (ColorPickerPreference)
                findPreference("battery_text_only_color");
        mCmCirleRingColor = (ColorPickerPreference)
                findPreference("battery_cmcircle_ring_color");
        mCmCirleRingColorCharge = (ColorPickerPreference)
                findPreference("battery_cmcircle_ring_color_charge");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        updateSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mBatteryBarChargingAnimation) {
            Settings.System.putInt(mContentResolver, Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryIcon) {
            int index = mBatteryIcon.findIndexOfValue((String) newValue);
            preference.setSummary(mBatteryIcon.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_ICON, val);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBar) {
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            preference.setSummary(mBatteryBar.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_BAR, val);
            return true;
        } else if (preference == mBatteryBarStyle) {
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            preference.setSummary(mBatteryBarStyle.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, val);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
            preference.setSummary(mBatteryBarThickness.getEntries()[index]);
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
            return true;
        } else if (preference == mBatteryTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mBatteryChargeTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BATTERY_CHARGE_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mCmCirleRingColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CMCIRLE_RING_COLOR, intHex);
            return true;
        } else if (preference == mCmCirleRingColorCharge) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_CMCIRLE_RING_COLOR_CHARGE, intHex);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mBatteryIcon.setOnPreferenceChangeListener(this);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);
        mBatteryTextColor.setOnPreferenceChangeListener(this);
        mBatteryChargeTextColor.setOnPreferenceChangeListener(this);
        mCmCirleRingColor.setOnPreferenceChangeListener(this);
        mCmCirleRingColorCharge.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mBatteryIcon.setValue((Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BATTERY_ICON, 0)) + "");
        mBatteryBar.setValue((Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBarStyle.setValue((Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);
        mBatteryBarThickness.setValue((Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1)) + "");
    }

    private void updateSummaries() {
        mBatteryIcon.setSummary(mBatteryIcon.getEntry());
        mBatteryBar.setSummary(mBatteryBar.getEntry());
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());
        mBatteryBarColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_BATTERY_BAR_COLOR, 0xFF0099CC)));
        mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());
        mBatteryChargeTextColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_BATTERY_CHARGE_TEXT_COLOR, 0xFF99CC00)));
        mBatteryTextColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_BATTERY_TEXT_COLOR, 0xFFFFFFFF)));
        mCmCirleRingColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_CMCIRLE_RING_COLOR, 0xFF0099CC)));
        mCmCirleRingColorCharge.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_CMCIRLE_RING_COLOR_CHARGE, 0xFF99CC00)));
    }
}
