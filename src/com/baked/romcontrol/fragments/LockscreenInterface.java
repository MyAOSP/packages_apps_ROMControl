/*
 * Copyright (C) 2012 CyanogenMod
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baked.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenInterface extends BAKEDPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenInterface";

    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "quick_unlock_control";
    private static final String KEY_VOLUME_WAKE = "volume_wake";
    private static final String KEY_VOLBTN_MUSIC_CTRL = "volbtn_music_controls";
    private static final String KEY_LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";

    private CheckBoxPreference mLockScreenRotation;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mVolBtnMusicCtrl;
    private CheckBoxPreference mLockMaximizeWidgets;
    private CheckBoxPreference mQuickUnlockScreen;
    private ColorPickerPreference mLockscreenTextColor;
    private ListPreference mBatteryStatus;

    private Activity mActivity;
    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mResolver = mActivity.getContentResolver();

        addPreferencesFromResource(R.xml.prefs_lockscreen);

        mVolumeWake = (CheckBoxPreference) findPreference(KEY_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(mResolver,
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        mVolBtnMusicCtrl = (CheckBoxPreference) findPreference(KEY_VOLBTN_MUSIC_CTRL);
        mVolBtnMusicCtrl.setChecked(Settings.System.getInt(mResolver,
                Settings.System.VOLBTN_MUSIC_CONTROLS, 0) == 1);

        mQuickUnlockScreen = (CheckBoxPreference) findPreference(LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        mQuickUnlockScreen.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);

        mLockMaximizeWidgets = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_MAXIMIZE_WIDGETS);
        if (Utils.isTablet(getActivity())) {
            getPreferenceScreen().removePreference(mLockMaximizeWidgets);
            mLockMaximizeWidgets = null;
        } else {
            mLockMaximizeWidgets.setOnPreferenceChangeListener(this);
        }

        mLockScreenRotation = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_ROTATION);
        mLockScreenRotation.setChecked(Settings.System.getInt(mResolver,
                Settings.System.LOCKSCREEN_AUTO_ROTATE, 0) == 1);

        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        mBatteryStatus.setOnPreferenceChangeListener(this);

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        ContentResolver cr = getActivity().getContentResolver();
        if (mBatteryStatus != null) {
            int batteryStatus = Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0);
            mBatteryStatus.setValueIndex(batteryStatus);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[batteryStatus]);
        }

        if (mLockMaximizeWidgets != null) {
            mLockMaximizeWidgets.setChecked(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, 0) == 1);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockScreenRotation) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;

        } else if (preference == mVolBtnMusicCtrl) {
            Settings.System.putInt(mResolver, Settings.System.VOLBTN_MUSIC_CONTROLS,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;

        } else if (preference == mVolumeWake) {
            Settings.System.putInt(mResolver, Settings.System.VOLUME_WAKE_SCREEN,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;

        } else if (preference == mQuickUnlockScreen) {
            Settings.System.putInt(mResolver, Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver cr = getActivity().getContentResolver();

        if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;

        } else if (preference == mLockMaximizeWidgets) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS, value ? 1 : 0);
            return true;

        } else if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }
}
