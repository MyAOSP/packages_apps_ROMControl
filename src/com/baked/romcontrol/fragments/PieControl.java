
package com.baked.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.Utils;
import com.baked.romcontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieControl extends BAKEDPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Pie Control";

    private static final String PIE_CONTROLS = "pie_controls";
    private static final String PIE_GRAVITY = "pie_gravity";
    private static final String PIE_MODE = "pie_mode";
    private static final String PIE_SIZE = "pie_size";
    private static final String PIE_TRIGGER = "pie_trigger";
    private static final String PIE_GAP = "pie_gap";
    private static final String PIE_LASTAPP = "pie_lastapp";
    private static final String PIE_MENU = "pie_menu";
    private static final String PIE_SEARCH = "pie_search";
    private static final String PIE_CENTER = "pie_center";
    private static final String PIE_STICK = "pie_stick";
    private static final String PIE_RESTART = "pie_restart_launcher";

    ListPreference mPieMode;
    ListPreference mPieSize;
    ListPreference mPieGravity;
    ListPreference mPieTrigger;
    ListPreference mPieGap;
    CheckBoxPreference mPieControls;
    CheckBoxPreference mPieMenu;
    CheckBoxPreference mPieLastApp;
    CheckBoxPreference mPieSearch;
    CheckBoxPreference mPieCenter;
    CheckBoxPreference mPieStick;
    CheckBoxPreference mPieRestart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.header_pie_control);
        addPreferencesFromResource(R.xml.prefs_pie_control);

        PreferenceScreen prefSet = getPreferenceScreen();

        mPieControls = (CheckBoxPreference) findPreference(PIE_CONTROLS);
        mPieGravity = (ListPreference) prefSet.findPreference(PIE_GRAVITY);
        mPieMode = (ListPreference) prefSet.findPreference(PIE_MODE);
        mPieSize = (ListPreference) prefSet.findPreference(PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PIE_TRIGGER);
        mPieGap = (ListPreference) prefSet.findPreference(PIE_GAP);
        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PIE_MENU);
        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PIE_LASTAPP);
        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PIE_SEARCH);
        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PIE_CENTER);
        mPieStick = (CheckBoxPreference) prefSet.findPreference(PIE_STICK);
        mPieRestart = (CheckBoxPreference) prefSet.findPreference(PIE_RESTART);
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
        if (preference == mPieControls) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_CONTROLS,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieMenu) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_MENU,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_LAST_APP,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_SEARCH,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_CENTER,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(mContentResolver, Settings.System.PIE_STICK,
                    checkBoxChecked(preference) ? 1 : 0);
        } else if (preference == mPieRestart) {
            Settings.System.putInt(mContentResolver, Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER,
                    checkBoxChecked(preference) ? 1 : 0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPieMode) {
            int index = mPieMode.findIndexOfValue((String) newValue);
            preference.setSummary(mPieMode.getEntries()[index]);
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_MODE, val);
            return true;
        } else if (preference == mPieSize) {
            int index = mPieSize.findIndexOfValue((String) newValue);
            preference.setSummary(mPieSize.getEntries()[index]);
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(mContentResolver, Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int index = mPieGravity.findIndexOfValue((String) newValue);
            preference.setSummary(mPieGravity.getEntries()[index]);
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_GRAVITY, val);
            return true;
        } else if (preference == mPieGap) {
            int index = mPieGap.findIndexOfValue((String) newValue);
            preference.setSummary(mPieGap.getEntries()[index]);
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver, Settings.System.PIE_GAP, val);
            return true;
        } else if (preference == mPieTrigger) {
            int index = mPieTrigger.findIndexOfValue((String) newValue);
            preference.setSummary(mPieTrigger.getEntries()[index]);
            float pieTrigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(mContentResolver, Settings.System.PIE_TRIGGER, pieTrigger);
            return true;
        }
        return false;
    }

    private void registerListeners() {
        mPieGravity.setOnPreferenceChangeListener(this);
        mPieMode.setOnPreferenceChangeListener(this);
        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);
        mPieGap.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mPieControls.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_CONTROLS, 0) == 1);
        mPieGravity.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_GRAVITY, 3)));
        mPieMode.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_MODE, 2)));
        try {
            float pieSize = Settings.System.getFloat(mContentResolver,
                    Settings.System.PIE_SIZE, 0.9f);
            mPieSize.setValue(String.valueOf(pieSize));

            float pieTrigger = Settings.System.getFloat(mContentResolver,
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch (Settings.SettingNotFoundException ex) {
            // oops
        }
        mPieRestart.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER, 0) == 1);
        mPieStick.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_STICK, 0) == 1);
        mPieCenter.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_CENTER, 1) == 1);
        mPieSearch.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_SEARCH, 0) == 1);
        mPieLastApp.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_LAST_APP, 0) == 1);
        mPieMenu.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_MENU, 0) == 1);
        mPieGap.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.PIE_GAP, 1)));
    }

    private void setSummaries() {
        mPieMode.setSummary(mPieMode.getEntry());
        mPieGravity.setSummary(mPieGravity.getEntry());
        mPieGap.setSummary(mPieGap.getEntry());
        mPieSize.setSummary(mPieSize.getEntry());
        mPieTrigger.setSummary(mPieTrigger.getEntry());
    }
}
