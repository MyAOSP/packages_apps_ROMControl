
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

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mRecentKillAll;
    CheckBoxPreference mKillAppLongpressBack;
    CheckBoxPreference mUseAltResolver;
    CheckBoxPreference mVibrateOnExpand;
    CheckBoxPreference mClockDateOpens;
    CheckBoxPreference mPluggedUnpluggedWakeup;
    CheckBoxPreference mHideExtras;
    ListPreference mUserModeUI;
    Preference mLcdDensity;

    Random randomGenerator = new Random();

    int newDensityValue;
    DensityChanger densityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_system_extra);

        PreferenceScreen prefs = getPreferenceScreen();

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }
        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);

        mKillAppLongpressBack = (CheckBoxPreference) findPreference(PREF_KILL_APP_LONGPRESS_BACK);
                updateKillAppLongpressBackOptions();

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        if (hasNavBarByDefault) {
            ((PreferenceGroup) findPreference("misc")).removePreference(mKillAppLongpressBack);
        }

        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.RECENT_KILL_ALL_BUTTON, false));

        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked()) {
            Resources res = mContext.getResources();
            String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
            int randomInt = randomGenerator.nextInt(insults.length);
            mDisableBootAnimation.setSummary(insults[randomInt]);
        }

        mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.VIBRATE_NOTIF_EXPAND, true));

        mClockDateOpens = (CheckBoxPreference) findPreference(PREF_CLOCK_DATE_OPENS);
        mClockDateOpens.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.CLOCK_DATE_OPENS, true));

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, true));

        mPluggedUnpluggedWakeup = (CheckBoxPreference) findPreference(PREF_PLUGGED_UNPLUGGED_WAKEUP);
        mPluggedUnpluggedWakeup.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));

        // hide option if device is already set to never wake up
        if(!mContext.getResources().getBoolean(com.android.internal.R.bool.config_unplugTurnsOnScreen)) {
            ((PreferenceGroup) findPreference("misc")).removePreference(mPluggedUnpluggedWakeup);
        }

        mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
        mHideExtras.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));

        mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
        int uiMode = Settings.System.getInt(mContentAppResolver,
                Settings.System.CURRENT_UI_MODE, 0);
        mUserModeUI.setValue(Integer.toString(Settings.System.getInt(mContentAppResolver,
                Settings.System.USER_UI_MODE, uiMode)));
        mUserModeUI.setOnPreferenceChangeListener(this);
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(mContentResolver, Settings.System.KILL_APP_LONGPRESS_BACK,
                mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
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
        }
        return false;
    }
}
