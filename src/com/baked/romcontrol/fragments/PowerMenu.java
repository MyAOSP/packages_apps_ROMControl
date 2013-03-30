
package com.baked.romcontrol.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;

public class PowerMenu extends BAKEDPreferenceFragment implements
            OnPreferenceChangeListener {

    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_POWER_OFF = "show_power_off";
    private static final String PREF_REBOOT_CHOOSER = "show_reboot_chooser";
    private static final String PREF_EXPANDED_DESKTOP = "expanded_desktop";
    private static final String PREF_NAVBAR_HIDE = "show_navbar_hide";
    private static final String PREF_AIRPLANE_TOGGLE = "show_airplane_toggle";
    private static final String PREF_SHOW_PROFILE_CHOOSER = "show_profile_chooser";
    private static final String PREF_SHOW_SOUND_CHOOSER = "show_sound_chooser";

    CheckBoxPreference mShowScreenShot;
    CheckBoxPreference mShowPowerOff;
    CheckBoxPreference mShowRebootChooser;
    CheckBoxPreference mShowAirplaneToggle;
    CheckBoxPreference mShowNavBarHide;
    CheckBoxPreference mShowProfileChooser;
    CheckBoxPreference mShowSoundChooser;
    ListPreference mExpandedDesktop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_powermenu);

        PreferenceScreen prefSet = getPreferenceScreen();
        mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowPowerOff = (CheckBoxPreference) findPreference(PREF_POWER_OFF);
        mShowRebootChooser = (CheckBoxPreference) findPreference(PREF_REBOOT_CHOOSER);
        mExpandedDesktop = (ListPreference) prefSet.findPreference(PREF_EXPANDED_DESKTOP);
        mShowAirplaneToggle = (CheckBoxPreference) findPreference(PREF_AIRPLANE_TOGGLE);
        mShowNavBarHide = (CheckBoxPreference) findPreference(PREF_NAVBAR_HIDE);
        mShowProfileChooser = (CheckBoxPreference) findPreference(PREF_SHOW_PROFILE_CHOOSER);
        mShowSoundChooser = (CheckBoxPreference) findPreference(PREF_SHOW_SOUND_CHOOSER);
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
        if (preference == mExpandedDesktop) {
            int index = mExpandedDesktop.findIndexOfValue((String) newValue);
            preference.setSummary(mExpandedDesktop.getEntries()[index]);
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.EXPANDED_DESKTOP_STYLE, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShowScreenShot) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowPowerOff) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_POWER_OFF,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowRebootChooser) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_REBOOT_CHOOSER,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowAirplaneToggle) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowNavBarHide) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowProfileChooser) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_PROFILE_CHOOSER,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowSoundChooser) {
            Settings.System.putInt(mContentResolver, Settings.System.POWER_DIALOG_SHOW_SOUND_CHOOSER,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void registerListeners() {
        mExpandedDesktop.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mShowScreenShot.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 0) == 1);
        mShowRebootChooser.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_REBOOT_CHOOSER, 1) == 1);
        mShowPowerOff.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_POWER_OFF, 1) == 1);
        mShowAirplaneToggle.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE, 1) == 1);
        mShowNavBarHide.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE, 0) == 1);
        mShowProfileChooser.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_PROFILE_CHOOSER, 0) == 1);
        mShowSoundChooser.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.POWER_DIALOG_SHOW_SOUND_CHOOSER, 1) == 1);
        mExpandedDesktop.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.EXPANDED_DESKTOP_STYLE, 0)));
    }

    private void updateSummaries() {
        mExpandedDesktop.setSummary(mExpandedDesktop.getEntry());
    }
}
