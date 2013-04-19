/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baked.romcontrol.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static com.android.internal.util.cm.QSConstants.TILE_BLUETOOTH;
import static com.android.internal.util.cm.QSConstants.TILE_FCHARGE;
import static com.android.internal.util.cm.QSConstants.TILE_LTE;
import static com.android.internal.util.cm.QSConstants.TILE_MOBILEDATA;
import static com.android.internal.util.cm.QSConstants.TILE_NETWORKMODE;
import static com.android.internal.util.cm.QSConstants.TILE_NFC;
import static com.android.internal.util.cm.QSConstants.TILE_PROFILE;
import static com.android.internal.util.cm.QSConstants.TILE_TORCH;
import static com.android.internal.util.cm.QSConstants.TILE_WIFIAP;
import static com.android.internal.util.cm.QSConstants.TILE_EXPANDEDDESKTOP;
import static com.android.internal.util.cm.QSUtils.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.Phone;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.Utils;
import com.baked.romcontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;

public class QuickSettings extends BAKEDPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "QuickSettings";

    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";
    private static final String EXP_RING_MODE = "pref_ring_mode";
    private static final String EXP_NETWORK_MODE = "pref_network_mode";
    private static final String EXP_SCREENTIMEOUT_MODE = "pref_screentimeout_mode";
    private static final String DYNAMIC_ALARM = "dynamic_alarm";
    private static final String DYNAMIC_BUGREPORT = "dynamic_bugreport";
    private static final String DYNAMIC_IME = "dynamic_ime";
    private static final String DYNAMIC_USBTETHER = "dynamic_usbtether";
    private static final String DYNAMIC_WIFI = "dynamic_wifi";
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String COLLAPSE_PANEL = "collapse_panel";
    private static final String GENERAL_SETTINGS = "pref_general_settings";
    private static final String STATIC_TILES = "static_tiles";
    private static final String DYNAMIC_TILES = "pref_dynamic_tiles";
    private static final String NUM_COLUMNS_PORT = "num_columns_port";
    private static final String NUM_COLUMNS_LAND = "num_columns_land";
    private static final String TILE_BACKGROUND_STYLE = "tile_background_style";
    private static final String TILE_BACKGROUND_COLOR = "tile_background_color";
    private static final String TILE_TEXT_COLOR = "tile_text_color";
    private static final String RANDOM_COLORS = "random_colors";
    private static final String FLIP_TILE = "flip_tile";

    CheckBoxPreference mDynamicAlarm;
    CheckBoxPreference mDynamicBugReport;
    CheckBoxPreference mDynamicUsbTether;
    CheckBoxPreference mDynamicWifi;
    CheckBoxPreference mDynamicIme;
    CheckBoxPreference mCollapsePanel;
    CheckBoxPreference mFlipTile;
    ColorPickerPreference mTileBgColor;
    ColorPickerPreference mTileTextColor;
    ListPreference mNumColumnsPort;
    ListPreference mTileBgStyle;
    ListPreference mNumColumnsLand;
    ListPreference mQuickPulldown;
    ListPreference mNetworkMode;
    ListPreference mScreenTimeoutMode;
    MultiSelectListPreference mRingMode;
    Preference mRandomColors;
    PreferenceCategory mGeneralSettings;
    PreferenceCategory mStaticTiles;
    PreferenceCategory mDynamicTiles;

    String mFastChargePath;

    String storedRingMode = Settings.System.getString(mContentResolver,
            Settings.System.EXPANDED_RING_MODE);

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        addPreferencesFromResource(R.xml.prefs_qs_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        PackageManager pm = getPackageManager();
        mRandomColors = (Preference) findPreference(RANDOM_COLORS);
        mGeneralSettings = (PreferenceCategory) prefSet.findPreference(GENERAL_SETTINGS);
        mStaticTiles = (PreferenceCategory) prefSet.findPreference(STATIC_TILES);
        mDynamicTiles = (PreferenceCategory) prefSet.findPreference(DYNAMIC_TILES);
        mQuickPulldown = (ListPreference) prefSet.findPreference(QUICK_PULLDOWN);
        if (!Utils.isPhone(getActivity())) {
            if (mQuickPulldown != null)
                mGeneralSettings.removePreference(mQuickPulldown);
        }

        mTileBgStyle = (ListPreference) findPreference(TILE_BACKGROUND_STYLE);
        mTileBgColor = (ColorPickerPreference) findPreference(TILE_BACKGROUND_COLOR);
        mTileTextColor = (ColorPickerPreference) findPreference(TILE_TEXT_COLOR);
        mNumColumnsPort = (ListPreference) prefSet.findPreference(NUM_COLUMNS_PORT);
        mNumColumnsLand = (ListPreference) prefSet.findPreference(NUM_COLUMNS_LAND);
        mCollapsePanel = (CheckBoxPreference) prefSet.findPreference(COLLAPSE_PANEL);
        mFlipTile = (CheckBoxPreference) prefSet.findPreference(FLIP_TILE);
        // Add the sound mode
        mRingMode = (MultiSelectListPreference) prefSet.findPreference(EXP_RING_MODE);
        // Add the network mode preference
        mNetworkMode = (ListPreference) prefSet.findPreference(EXP_NETWORK_MODE);
        // Screen timeout mode
        mScreenTimeoutMode = (ListPreference) prefSet.findPreference(EXP_SCREENTIMEOUT_MODE);
        // Add the dynamic tiles checkboxes
        mDynamicAlarm = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_ALARM);
        mDynamicBugReport = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_BUGREPORT);
        mDynamicIme = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_IME);
        mDynamicUsbTether = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_USBTETHER);

        if (mDynamicUsbTether != null) {
            if (deviceSupportsUsbTether(getActivity())) {
                mDynamicUsbTether.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.QS_DYNAMIC_USBTETHER, 1) == 1);
            } else {
                mDynamicTiles.removePreference(mDynamicUsbTether);
                mDynamicUsbTether = null;
            }
        }
        mDynamicWifi = (CheckBoxPreference) prefSet.findPreference(DYNAMIC_WIFI);
        if (mDynamicWifi != null) {
            if (deviceSupportsWifiDisplay(getActivity())) {
                mDynamicWifi.setChecked(Settings.System.getInt(mContentResolver,
                        Settings.System.QS_DYNAMIC_WIFI, 1) == 1);
            } else {
                mDynamicTiles.removePreference(mDynamicWifi);
                mDynamicWifi = null;
            }
        }

        // Don't show mobile data options if not supported
        if (!deviceSupportsMobileData(mContext)) {
            QuickSettingsUtil.TILES.remove(TILE_MOBILEDATA);
            QuickSettingsUtil.TILES.remove(TILE_WIFIAP);
            QuickSettingsUtil.TILES.remove(TILE_NETWORKMODE);
            if(mNetworkMode != null) {
                mStaticTiles.removePreference(mNetworkMode);
            }
        } else {
            // We have telephony support however, some phones run on networks not supported
            // by the networkmode tile so remove both it and the associated options list
            int network_state = -99;
            try {
                network_state = Settings.Global.getInt(mContentResolver,
                        Settings.Global.PREFERRED_NETWORK_MODE);
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "Unable to retrieve PREFERRED_NETWORK_MODE", e);
            }

            switch (network_state) {
                // list of supported network modes
                case Phone.NT_MODE_WCDMA_PREF:
                case Phone.NT_MODE_WCDMA_ONLY:
                case Phone.NT_MODE_GSM_UMTS:
                case Phone.NT_MODE_GSM_ONLY:
                    break;
                default:
                    QuickSettingsUtil.TILES.remove(TILE_NETWORKMODE);
                    mStaticTiles.removePreference(mNetworkMode);
                    break;
            }
        }

        // Don't show the bluetooth options if not supported
        if (!deviceSupportsBluetooth()) {
            QuickSettingsUtil.TILES.remove(TILE_BLUETOOTH);
        }

        // Don't show the profiles tile if profiles are disabled
        if (!systemProfilesEnabled(mContentResolver)) {
            QuickSettingsUtil.TILES.remove(TILE_PROFILE);
        }

        // Don't show the NFC tile if not supported
        if (!deviceSupportsNfc(getActivity())) {
            QuickSettingsUtil.TILES.remove(TILE_NFC);
        }

        mFastChargePath = getActivity().getApplicationContext().getString(
                com.android.internal.R.string.config_fastChargePath);

        if (!new File(mFastChargePath).exists() || !deviceSupportsFastCharge(getActivity())) {
            QuickSettingsUtil.TILES.remove(TILE_FCHARGE);
        }

        // Don't show the LTE tile if not supported
        if (!deviceSupportsLte(getActivity())) {
            QuickSettingsUtil.TILES.remove(TILE_LTE);
        }

        // Don't show Torch tile if not supported
        if (!hasTorch) {
            QuickSettingsUtil.TILES.remove(TILE_TORCH);
        }

        // Don't show the Expanded desktop tile if expanded desktop is disabled
        if (!expandedDesktopEnabled(mContentResolver)) {
            QuickSettingsUtil.TILES.remove(TILE_EXPANDEDDESKTOP);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        updateSummaries();
        updateVisibility();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDynamicAlarm) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_DYNAMIC_ALARM,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mDynamicBugReport) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_DYNAMIC_BUGREPORT,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mDynamicIme) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_DYNAMIC_IME,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (mDynamicUsbTether != null && preference == mDynamicUsbTether) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_DYNAMIC_USBTETHER,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (mDynamicWifi != null && preference == mDynamicWifi) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_DYNAMIC_WIFI,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mCollapsePanel) {
            Settings.System.putInt(mContentResolver, Settings.System.QS_COLLAPSE_PANEL,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mFlipTile) {
            Settings.System.putInt(mContentResolver, Settings.System.QUICK_SETTINGS_TILES_FLIP,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mRandomColors) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            RandomColors fragment = new RandomColors();
            ft.addToBackStack("pick_random_colors");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRingMode) {
            ArrayList<String> arrValue = new ArrayList<String>((Set<String>) newValue);
            Collections.sort(arrValue, new MultiSelectListPreferenceComparator(mRingMode));
            Settings.System.putString(mContentResolver, Settings.System.EXPANDED_RING_MODE,
                    TextUtils.join(SEPARATOR, arrValue));
            updateSummary(TextUtils.join(SEPARATOR, arrValue),
                    mRingMode, R.string.pref_ring_mode_summary);
            return true;
        } else if (preference == mNumColumnsPort) {
            int val = Integer.parseInt((String) newValue);
            int index = mNumColumnsPort.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, val);
            preference.setSummary(mNumColumnsPort.getEntries()[index]);
            return true;
        } else if (preference == mNumColumnsLand) {
            int val = Integer.parseInt((String) newValue);
            int index = mNumColumnsLand.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, val);
            preference.setSummary(mNumColumnsLand.getEntries()[index]);
            return true;
        } else if (preference == mNetworkMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mNetworkMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.EXPANDED_NETWORK_MODE, value);
            preference.setSummary(mNetworkMode.getEntries()[index]);
            return true;
       } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.QS_QUICK_PULLDOWN,
                    quickPulldownValue);
            updatePulldownSummary(quickPulldownValue);
            return true;
      } else if (preference == mScreenTimeoutMode) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenTimeoutMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentAppResolver,
                    Settings.System.EXPANDED_SCREENTIMEOUT_MODE, value);
            preference.setSummary(mScreenTimeoutMode.getEntries()[index]);
            return true;
        } else if (preference == mTileTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mTileBgColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, intHex);
            return true;
        } else if (preference == mTileBgStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mTileBgStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentAppResolver,
                    Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, value);
            preference.setSummary(mTileBgStyle.getEntries()[index]);
            updateVisibility();
            return true;
        }
        return false;
    }

    private class MultiSelectListPreferenceComparator implements Comparator<String> {
        private MultiSelectListPreference pref;

        MultiSelectListPreferenceComparator(MultiSelectListPreference p) {
            pref = p;
        }

        @Override
        public int compare(String lhs, String rhs) {
            return Integer.compare(pref.findIndexOfValue(lhs),
                    pref.findIndexOfValue(rhs));
        }
    }

    private void registerListeners() {
        if (Utils.isPhone(getActivity())) {
            mQuickPulldown.setOnPreferenceChangeListener(this);
        }
        mTileBgStyle.setOnPreferenceChangeListener(this);
        mTileBgColor.setOnPreferenceChangeListener(this);
        mTileTextColor.setOnPreferenceChangeListener(this);
        mNumColumnsPort.setOnPreferenceChangeListener(this);
        mNumColumnsLand.setOnPreferenceChangeListener(this);
        mRingMode.setOnPreferenceChangeListener(this);
        mScreenTimeoutMode.setOnPreferenceChangeListener(this);
        if (mNetworkMode != null) {
            mNetworkMode.setOnPreferenceChangeListener(this);
        }

    }

    private void setDefaultValues() {
        mQuickPulldown.setValue(Integer.toString(Settings.System.getInt(
                mContentResolver, Settings.System.QS_QUICK_PULLDOWN, 0)));
        mTileBgStyle.setValue(Integer.toString(Settings.System.getInt(
                mContentResolver, Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2)));
        mNumColumnsPort.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.QUICK_SETTINGS_NUM_COLUMNS_PORT, 3)));
        mNumColumnsLand.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.QUICK_SETTINGS_NUM_COLUMNS_LAND, 5)));
        mCollapsePanel.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.QS_COLLAPSE_PANEL, 0) == 1);
        mFlipTile.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 1) == 1);
        mDynamicAlarm.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.QS_DYNAMIC_ALARM, 1) == 1);
        mDynamicBugReport.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.QS_DYNAMIC_BUGREPORT, 1) == 1);
        mDynamicIme.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.QS_DYNAMIC_IME, 1) == 1);
        mScreenTimeoutMode.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.EXPANDED_SCREENTIMEOUT_MODE, 1)));
        if (storedRingMode != null) {
            String[] ringModeArray = TextUtils.split(storedRingMode, SEPARATOR);
            mRingMode.setValues(new HashSet<String>(Arrays.asList(ringModeArray)));
        }
    }

    private void updateSummaries() {
        updatePulldownSummary(Settings.System.getInt(
                mContentResolver, Settings.System.QS_QUICK_PULLDOWN, 0));
        mTileBgStyle.setSummary(mTileBgStyle.getEntry());
        mNumColumnsPort.setSummary(mNumColumnsPort.getEntry());
        mNumColumnsLand.setSummary(mNumColumnsLand.getEntry());
        mScreenTimeoutMode.setSummary(mScreenTimeoutMode.getEntry());
        mTileBgColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.QUICK_SETTINGS_BACKGROUND_COLOR, 0xFF000000)));
        mTileTextColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.QUICK_SETTINGS_TEXT_COLOR, 0xFFFFFFFF)));
        if (mNetworkMode != null) {
            mNetworkMode.setSummary(mNetworkMode.getEntry());
        }
        if (storedRingMode != null) {
            updateSummary(storedRingMode, mRingMode, R.string.pref_ring_mode_summary);
        }
    }

    private void updateVisibility() {
        int visible = Settings.System.getInt(mContentResolver,
                    Settings.System.QUICK_SETTINGS_BACKGROUND_STYLE, 2);
        if (visible == 2) {
            mRandomColors.setEnabled(false);
            mTileBgColor.setEnabled(false);
        } else if (visible == 1) {
            mRandomColors.setEnabled(false);
            mTileBgColor.setEnabled(true);
        } else {
            mRandomColors.setEnabled(true);
            mTileBgColor.setEnabled(false);
        }
    }

    private void updateSummary(String val, MultiSelectListPreference pref, int defSummary) {
        // Update summary message with current values
        final String[] values = parseStoredValue(val);
        if (values != null) {
            final int length = values.length;
            final CharSequence[] entries = pref.getEntries();
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < (length); i++) {
                CharSequence entry = entries[Integer.parseInt(values[i])];
                if ((length - i) > 1) {
                    summary.append(entry).append(" | ");
                } else {
                    summary.append(entry);
                }
            }
            pref.setSummary(summary);
        } else {
            pref.setSummary(defSummary);
        }
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            /* quick pulldown deactivated */
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_summary_left
                    : R.string.quick_pulldown_summary_right);
            mQuickPulldown.setSummary(res.getString(R.string.summary_quick_pulldown, direction));
        }
    }

    public static String[] parseStoredValue(CharSequence val) {
        if (TextUtils.isEmpty(val)) {
            return null;
        } else {
            return val.toString().split(SEPARATOR);
        }
    }
}
