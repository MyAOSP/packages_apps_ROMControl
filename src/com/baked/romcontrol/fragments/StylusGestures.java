
package com.baked.romcontrol.fragments;

import static com.android.internal.util.action.ActionConstants.*;
import com.android.internal.util.action.NavBarHelpers;

import java.net.URISyntaxException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.R;
import com.baked.romcontrol.util.ShortcutPickerHelper;

public class StylusGestures extends BAKEDPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    ListPreference mLeft;
    ListPreference mRight;
    ListPreference mUp;
    ListPreference mDown;
    ListPreference mDouble;
    ListPreference mLong;
    CheckBoxPreference mEnableSPen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.header_stylus_gestures);
        addPreferencesFromResource(R.xml.prefs_stylus_gestures);

        mPicker = new ShortcutPickerHelper(this, this);

        mEnableSPen = (CheckBoxPreference) findPreference("enable_spen");
        mLeft = (ListPreference) findPreference("spen_left");
        mRight = (ListPreference) findPreference("spen_right");
        mUp = (ListPreference) findPreference("spen_up");
        mDown = (ListPreference) findPreference("spen_down");
        mDouble = (ListPreference) findPreference("spen_double");
        mLong = (ListPreference) findPreference("spen_long");

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
        if (preference == mEnableSPen) {
            Settings.System.putBoolean(mContentResolver, Settings.System.ENABLE_SPEN_ACTIONS,
                    checkBoxChecked(preference));
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mLeft) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_LEFT];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[SWIPE_LEFT], (String) newValue);
            mLeft.setSummary(getProperSummary(mLeft));
            }

        } else if (preference == mRight) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_RIGHT];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[SWIPE_RIGHT], (String) newValue);
            mRight.setSummary(getProperSummary(mRight));
            }
        } else if (preference == mUp) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_UP];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[SWIPE_UP], (String) newValue);
            mUp.setSummary(getProperSummary(mUp));
            }
        } else if (preference == mDown) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_DOWN];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[SWIPE_DOWN], (String) newValue);
            mDown.setSummary(getProperSummary(mDown));
            }
        } else if (preference == mDouble) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[TAP_DOUBLE];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[TAP_DOUBLE], (String) newValue);
            mDouble.setSummary(getProperSummary(mDouble));
            }
        } else if (preference == mLong) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[PRESS_LONG];
            if (newValue.equals(ActionConstant.ACTION_APP.value())) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(mContentResolver,
                       Settings.System.SPEN_ACTIONS[PRESS_LONG], (String) newValue);
            mLong.setSummary(getProperSummary(mLong));
            }
        }
        return result;
    }

    private void registerListeners() {
        mLeft.setOnPreferenceChangeListener(this);
        mRight.setOnPreferenceChangeListener(this);
        mUp.setOnPreferenceChangeListener(this);
        mDown.setOnPreferenceChangeListener(this);
        mDouble.setOnPreferenceChangeListener(this);
        mLong.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mEnableSPen.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.ENABLE_SPEN_ACTIONS, false));
    }

    private void updateSummaries() {
        mLeft.setSummary(getProperSummary(mLeft));
        mRight.setSummary(getProperSummary(mRight));
        mUp.setSummary(getProperSummary(mUp));
        mDown.setSummary(getProperSummary(mDown));
        mDouble.setSummary(getProperSummary(mDouble));
        mLong.setSummary(getProperSummary(mLong));
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
          mPreference.setSummary(friendlyName);
          Settings.System.putString(getContentResolver(), mString, (String) uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mLeft) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_LEFT];
        } else if (preference == mRight) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_RIGHT];
        } else if (preference == mUp) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_UP];
        } else if (preference == mDown) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_DOWN];
        } else if (preference == mDouble) {
            mString = Settings.System.SPEN_ACTIONS[TAP_DOUBLE];
        } else if (preference == mLong) {
            mString = Settings.System.SPEN_ACTIONS[PRESS_LONG];
        }

        String uri = Settings.System.getString(mContentResolver, mString);
        return NavBarHelpers.getProperSummary(mContext, uri);
    }
}
