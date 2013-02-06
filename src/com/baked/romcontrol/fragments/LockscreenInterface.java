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
import net.margaritov.preference.colorpicker.ColorPickerView;

public class LockscreenInterface extends BAKEDPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenInterface";
    public static final int REQUEST_PICK_WALLPAPER = 199;
    public static final int SELECT_WALLPAPER = 3;

    private static final int LOCKSCREEN_BACKGROUND_COLOR_FILL = 0;
    private static final int LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE = 1;
    private static final int LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER = 2;

    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "quick_unlock_control";
    private static final String KEY_VOLUME_WAKE = "volume_wake";
    private static final String KEY_VOLBTN_MUSIC_CTRL = "volbtn_music_controls";
    private static final String KEY_LOCKSCREEN_MAXIMIZE_WIDGETS = "lockscreen_maximize_widgets";
    private static final String KEY_LOCKSCREEN_BACKGROUND_ALPHA = "lockscreen_background_alpha";
    public static final String KEY_BACKGROUND_PREF = "lockscreen_background";

    private File mWallpaperImage;
    private File mWallpaperTemporary;
    private ListPreference mCustomBackground;

    private CheckBoxPreference mLockScreenRotation;
    private CheckBoxPreference mVolumeWake;
    private CheckBoxPreference mVolBtnMusicCtrl;
    private CheckBoxPreference mLockMaximizeWidgets;
    private CheckBoxPreference mQuickUnlockScreen;
    private ColorPickerPreference mLockscreenTextColor;
    private ListPreference mBatteryStatus;
    private Preference mWallpaperAlpha;

    private Activity mActivity;
    private ContentResolver mResolver;

    private int seekbarProgress;

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
        if (!Utils.isPhone(getActivity())) {
            getPreferenceScreen().removePreference(mLockMaximizeWidgets);
            mLockMaximizeWidgets = null;
        } else {
            mLockMaximizeWidgets.setOnPreferenceChangeListener(this);
        }

        mLockScreenRotation = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_ROTATION);
        mLockScreenRotation.setChecked(Settings.System.getBoolean(mResolver,
                Settings.System.LOCKSCREEN_AUTO_ROTATE, false));

        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        mBatteryStatus.setOnPreferenceChangeListener(this);

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

        mCustomBackground = (ListPreference) findPreference(KEY_BACKGROUND_PREF);
        mCustomBackground.setOnPreferenceChangeListener(this);
        updateCustomBackgroundSummary();

        mWallpaperImage = new File(getActivity().getFilesDir() + "/lockwallpaper");
        mWallpaperTemporary = new File(getActivity().getCacheDir() + "/lockwallpaper.tmp");

        mWallpaperAlpha = (Preference) findPreference(KEY_LOCKSCREEN_BACKGROUND_ALPHA);
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND);
        if (value == null) {
            resId = R.string.lockscreen_background_default_wallpaper;
            mCustomBackground.setValueIndex(2);
        } else if (value.isEmpty()) {
            resId = R.string.lockscreen_background_custom_image;
            mCustomBackground.setValueIndex(1);
        } else {
            resId = R.string.lockscreen_background_color_fill;
            mCustomBackground.setValueIndex(0);
        }
        mCustomBackground.setSummary(getResources().getString(resId));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.renameTo(mWallpaperImage);
                }
                mWallpaperImage.setReadable(true, false);
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND,"");
                updateCustomBackgroundSummary();
            } else {
                if (mWallpaperTemporary.exists()) {
                    mWallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
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
            Settings.System.putBoolean(getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    checkBoxChecked(preference));
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

        } else if (preference == mWallpaperAlpha) {
            Resources res = getActivity().getResources();
            String cancel = res.getString(R.string.cancel);
            String ok = res.getString(R.string.ok);
            String title = res.getString(R.string.alpha_dialog_title);
            float savedProgress = Settings.System.getFloat(getActivity()
                        .getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND_ALPHA, 1.0f);

            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
            SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
            OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                    seekbarProgress = seekbar.getProgress();
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekbar) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekbar) {
                }
            };
            seekbar.setProgress((int) (savedProgress * 100));
            seekbar.setMax(100);
            seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setView(alphaDialog)
                    .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // nothing
                }
            })
            .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    float val = ((float) seekbarProgress / 100);
                    Settings.System.putFloat(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND_ALPHA, val);
                }
            })
            .create()
            .show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver cr = getActivity().getContentResolver();

        if (preference == mCustomBackground) {
            int selection = mCustomBackground.findIndexOfValue(objValue.toString());
            return handleBackgroundSelection(selection);

        } else if (preference == mBatteryStatus) {
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

    private boolean handleBackgroundSelection(int selection) {
        if (selection == LOCKSCREEN_BACKGROUND_COLOR_FILL) {
            final ColorPickerView colorView = new ColorPickerView(getActivity());
            int currentColor = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND, -1);

            if (currentColor != -1) {
                colorView.setColor(currentColor);
            }
            colorView.setAlphaSliderVisible(true);

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.lockscreen_custom_background_dialog_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContentResolver(),
                                    Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                            updateCustomBackgroundSummary();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(colorView)
                    .show();
        } else if (selection == LOCKSCREEN_BACKGROUND_CUSTOM_IMAGE) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            final Display display = getActivity().getWindowManager().getDefaultDisplay();
            final Rect rect = new Rect();
            final Window window = getActivity().getWindow();

            window.getDecorView().getWindowVisibleDisplayFrame(rect);

            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            boolean isPortrait = getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT;

            int width = display.getWidth();
            int height = display.getHeight() - titleBarHeight;

            intent.putExtra("aspectX", isPortrait ? width : height);
            intent.putExtra("aspectY", isPortrait ? height : width);

            try {
                mWallpaperTemporary.createNewFile();
                mWallpaperTemporary.setWritable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mWallpaperTemporary));
                intent.putExtra("return-data", false);
                getActivity().startActivityFromFragment(this, intent, REQUEST_PICK_WALLPAPER);
            } catch (IOException e) {
                // Do nothing here
            } catch (ActivityNotFoundException e) {
                // Do nothing here
            }
        } else if (selection == LOCKSCREEN_BACKGROUND_DEFAULT_WALLPAPER) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_BACKGROUND, null);
            updateCustomBackgroundSummary();
            return true;
        }

        return false;
    }
}
