
package com.baked.romcontrol.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.Utils;
import com.baked.romcontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieColor extends BAKEDPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PIE_ENABLE_COLOR = "pie_enable_color";
    private static final String PIE_JUICE = "pie_juice";
    private static final String PIE_BACKGROUND = "pie_background";
    private static final String PIE_SELECT = "pie_select";
    private static final String PIE_OUTLINES = "pie_outlines";
    private static final String PIE_STATUS_CLOCK = "pie_status_clock";
    private static final String PIE_STATUS = "pie_status";
    private static final String PIE_CHEVRON_LEFT = "pie_chevron_left";
    private static final String PIE_CHEVRON_RIGHT = "pie_chevron_right";
    private static final String PIE_BUTTON_COLOR = "pie_button_color";

    CheckBoxPreference mEnableColor;
    ColorPickerPreference mPieBg;
    ColorPickerPreference mJuice;
    ColorPickerPreference mSelect;
    ColorPickerPreference mOutlines;
    ColorPickerPreference mStatusClock;
    ColorPickerPreference mStatus;
    ColorPickerPreference mChevronLeft;
    ColorPickerPreference mChevronRight;
    ColorPickerPreference mBtnColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_pie_color);

        mEnableColor = (CheckBoxPreference) findPreference(PIE_ENABLE_COLOR);
        mPieBg = (ColorPickerPreference) findPreference(PIE_BACKGROUND);
        mJuice = (ColorPickerPreference) findPreference(PIE_JUICE);
        mSelect = (ColorPickerPreference) findPreference(PIE_SELECT);
        mOutlines = (ColorPickerPreference) findPreference(PIE_OUTLINES);
        mStatusClock = (ColorPickerPreference) findPreference(PIE_STATUS_CLOCK);
        mStatus = (ColorPickerPreference) findPreference(PIE_STATUS);
        mChevronLeft = (ColorPickerPreference) findPreference(PIE_CHEVRON_LEFT);
        mChevronRight = (ColorPickerPreference) findPreference(PIE_CHEVRON_RIGHT);
        mBtnColor = (ColorPickerPreference) findPreference(PIE_BUTTON_COLOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        setSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableColor) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_ENABLE_COLOR,
                    checkBoxChecked(preference) ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieBg) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_BACKGROUND, intHex);
            return true;
        } else if (preference == mSelect) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_SELECT, intHex);
            return true;
        } else if (preference == mOutlines) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_OUTLINES, intHex);
            return true;
        } else if (preference == mStatusClock) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_STATUS_CLOCK, intHex);
            return true;
        } else if (preference == mStatus) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_STATUS, intHex);
            return true;
        } else if (preference == mChevronLeft) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_CHEVRON_LEFT, intHex);
            return true;
        } else if (preference == mChevronRight) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_CHEVRON_RIGHT, intHex);
            return true;
        } else if (preference == mBtnColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_BUTTON_COLOR, intHex);
            return true;
        } else if (preference == mJuice) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_JUICE, intHex);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mPieBg.setOnPreferenceChangeListener(this);
        mJuice.setOnPreferenceChangeListener(this);
        mSelect.setOnPreferenceChangeListener(this);
        mOutlines.setOnPreferenceChangeListener(this);
        mStatusClock.setOnPreferenceChangeListener(this);
        mStatus.setOnPreferenceChangeListener(this);
        mChevronLeft.setOnPreferenceChangeListener(this);
        mChevronRight.setOnPreferenceChangeListener(this);
        mBtnColor.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mEnableColor.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_ENABLE_COLOR, 0) == 1);
    }

    private void setSummaries() {
        mPieBg.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_BACKGROUND, 0xF00099CC)));
        mJuice.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_JUICE, 0xFF33B5E5)));
        mSelect.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_SELECT, 0xAADFDFDF)));
        mOutlines.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_OUTLINES, 0x550099CC)));
        mStatusClock.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_STATUS_CLOCK, 0xFFFFFFFF)));
        mStatus.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_STATUS, 0xFFFFFFFF)));
        mChevronLeft.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_CHEVRON_LEFT, 0xF00099CC)));
        mChevronRight.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_CHEVRON_RIGHT, 0xF00099CC)));
        mBtnColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.PIE_BUTTON_COLOR, 0xB2FFFFFF)));
    }
}
