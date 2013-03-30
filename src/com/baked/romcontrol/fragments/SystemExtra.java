
package com.baked.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.util.CMDProcessor;
import com.baked.romcontrol.util.Helpers;

public class SystemExtra extends BAKEDPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String TAG = "SystemExtra";

    private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
    private static final String PREF_KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final String PREF_CLOCK_DATE_OPENS = "clock_date_opens";
    private static final String PREF_PLUGGED_UNPLUGGED_WAKEUP = "plugged_unplugged_wakeup";
    private static final String PREF_USER_MODE_UI = "user_mode_ui";
    private static final String PREF_HIDE_EXTRAS = "hide_extras";
    private static final String PREF_POWER_CRT_MODE = "system_power_crt_mode";
    private static final String PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mRecentKillAll;
    CheckBoxPreference mKillAppLongpressBack;
    CheckBoxPreference mUseAltResolver;
    CheckBoxPreference mVibrateOnExpand;
    CheckBoxPreference mClockDateOpens;
    CheckBoxPreference mPluggedUnpluggedWakeup;
    CheckBoxPreference mHideExtras;
    CheckBoxPreference mCrtOff;
    ListPreference mCrtMode;
    ListPreference mUserModeUI;
    Preference mLcdDensity;

    Random randomGenerator = new Random();
    String currentProperty = SystemProperties.get("ro.sf.lcd_density");

    int newDensityValue;
    DensityChanger densityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_system_extra);

        PreferenceScreen prefs = getPreferenceScreen();

        mLcdDensity = findPreference("lcd_density_setup");
        mKillAppLongpressBack = (CheckBoxPreference) findPreference(PREF_KILL_APP_LONGPRESS_BACK);
        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mClockDateOpens = (CheckBoxPreference) findPreference(PREF_CLOCK_DATE_OPENS);
        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mPluggedUnpluggedWakeup = (CheckBoxPreference) findPreference(PREF_PLUGGED_UNPLUGGED_WAKEUP);
        mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
        mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
        mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
        mCrtMode = (ListPreference) findPreference(PREF_POWER_CRT_MODE);

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        if (hasNavBarByDefault) {
            ((PreferenceGroup) findPreference("misc")).removePreference(mKillAppLongpressBack);
        }
        // hide option if device is already set to never wake up
        if(!mContext.getResources().getBoolean(com.android.internal.R.bool.config_unplugTurnsOnScreen)) {
            ((PreferenceGroup) findPreference("misc")).removePreference(mPluggedUnpluggedWakeup);
        }
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }
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
        if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.baked");
                Helpers.getMount("ro");
                Resources res = mContext.getResources();
                String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                int randomInt = randomGenerator.nextInt(insults.length);
                preference.setSummary(insults[randomInt]);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.baked /system/media/bootanimation.zip");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;
        } else if (preference == mKillAppLongpressBack) {
            writeKillAppLongpressBackOptions();
        } else if (preference == mRecentKillAll) {
            Settings.System.putBoolean(mContentResolver, Settings.System.RECENT_KILL_ALL_BUTTON,
                    checkBoxChecked(preference));
            return true;
        } else if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
                    .startPreferenceFragment(new DensityChanger(), true);
            return true;
        } else if (preference == mUseAltResolver) {
            Settings.System.putBoolean(mContentResolver, Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                    checkBoxChecked(preference));
            return true;
        } else if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContentResolver, Settings.System.VIBRATE_NOTIF_EXPAND,
                    checkBoxChecked(preference));
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mClockDateOpens) {
            Settings.System.putBoolean(mContentResolver, Settings.System.CLOCK_DATE_OPENS,
                    checkBoxChecked(preference));
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mPluggedUnpluggedWakeup) {
            Settings.System.putBoolean(mContentResolver, Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED,
                    checkBoxChecked(preference));
        } else if (preference == mHideExtras) {
            Settings.System.putBoolean(mContentResolver, Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
                    checkBoxChecked(preference));
            return true;
        } else if (preference == mCrtOff) {
            Settings.System.putBoolean(mContentResolver, Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                    checkBoxChecked(preference));
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUserModeUI) {
            Settings.System.putInt(mContentResolver,
                    Settings.System.USER_UI_MODE, Integer.parseInt((String) newValue));
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mCrtMode) {
            int index = mCrtMode.findIndexOfValue((String) newValue);
            preference.setSummary(mCrtMode.getEntries()[index]);
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.SYSTEM_POWER_CRT_MODE, val);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mUserModeUI.setOnPreferenceChangeListener(this);
        mCrtMode.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mRecentKillAll.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.RECENT_KILL_ALL_BUTTON, false));
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.VIBRATE_NOTIF_EXPAND, true));
        mClockDateOpens.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.CLOCK_DATE_OPENS, true));
        mUseAltResolver.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, true));
        mPluggedUnpluggedWakeup.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));
        mHideExtras.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));
        mUserModeUI.setValue(Integer.toString(Settings.System.getInt(mContentAppResolver,
                Settings.System.USER_UI_MODE, 0)));
        mCrtOff.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF, true));
        mCrtMode.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.SYSTEM_POWER_CRT_MODE, 0)));
        updateKillAppLongpressBackOptions();
    }

    private void updateSummaries() {
        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);
        mCrtMode.setSummary(mCrtMode.getEntry());
        if (mDisableBootAnimation.isChecked()) {
            Resources res = mContext.getResources();
            String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
            int randomInt = randomGenerator.nextInt(insults.length);
            mDisableBootAnimation.setSummary(insults[randomInt]);
        }
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(mContentResolver, Settings.System.KILL_APP_LONGPRESS_BACK,
                mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
    }
}
