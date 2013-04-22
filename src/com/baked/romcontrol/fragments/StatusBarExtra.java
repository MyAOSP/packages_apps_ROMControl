
package com.baked.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.util.CMDProcessor;
import com.baked.romcontrol.util.Helpers;
import com.baked.romcontrol.widgets.SeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;

public class StatusBarExtra extends BAKEDPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "StatusBarExtra";

    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
    private static final String PREF_EXPANDED_CLOCK_COLOR = "expanded_clock_color";
    private static final String PREF_STATUSBAR_BACKGROUND_STYLE = "statusbar_background_style";
    private static final String PREF_STATUSBAR_BACKGROUND_COLOR = "statusbar_background_color";
    private static final String PREF_STATUSBAR_BRIGHTNESS_SLIDER = "statusbar_brightness_slider";

    private static final int REQUEST_PICK_WALLPAPER = 201;

    private static final int NOTIF_BACKGROUND_COLOR_FILL = 0;
    private static final int NOTIF_BACKGROUND_CUSTOM_IMAGE = 1;
    private static final int NOTIF_BACKGROUND_DEFAULT = 2;

    CheckBoxPreference mStatusBarNotifCount;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mStatusBarBrightnessSlider;
    Preference mCustomLabel;
    ListPreference mNotificationBackground;
    ListPreference mStatusbarBgStyle;
    SeekBarPreference mWallpaperAlpha;
    ColorPickerPreference mExpandedClockColor;
    ColorPickerPreference mStatusbarBgColor;

    private Activity mActivity;
    private File wallpaperImage;
    private File wallpaperTemporary;

    private int seekbarProgress;

    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        addPreferencesFromResource(R.xml.prefs_statusbar_extra);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarBrightnessSlider = (CheckBoxPreference) findPreference(PREF_STATUSBAR_BRIGHTNESS_SLIDER);
        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        mNotificationBackground = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER);
        mWallpaperAlpha = (SeekBarPreference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);
        mExpandedClockColor = (ColorPickerPreference) findPreference(PREF_EXPANDED_CLOCK_COLOR);
        mStatusbarBgColor = (ColorPickerPreference) findPreference(PREF_STATUSBAR_BACKGROUND_COLOR);
        mStatusbarBgStyle = (ListPreference) findPreference(PREF_STATUSBAR_BACKGROUND_STYLE);

        wallpaperImage = new File(mActivity.getFilesDir()+"/notifwallpaper");
        wallpaperTemporary = new File(mActivity.getCacheDir()+"/notifwallpaper.tmp");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListeners();
        setDefaultValues();
        updateSummaries();
        updateCustomLabelTextSummary();
        updateVisibility();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(mContentResolver, Settings.System.STATUSBAR_NOTIF_COUNT,
                    checkBoxChecked(preference) ? 1 : 0);
            return true;
        } else if (preference == mShowImeSwitcher) {
            Settings.System.putBoolean(mContentResolver, Settings.System.SHOW_STATUSBAR_IME_SWITCHER,
                    checkBoxChecked(preference));
            return true;
        } else if (preference == mStatusBarBrightnessSlider) {
            Settings.System.putBoolean(mContentResolver, Settings.System.STATUSBAR_BRIGHTNESS_CONTROL,
                    checkBoxChecked(preference));
            return true;
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);
            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(mContentResolver,
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.baked.romcontrol.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mExpandedClockColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_EXPANDED_CLOCK_COLOR, intHex);
            return true;
        } else if (preference == mStatusbarBgStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mStatusbarBgStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(mContentAppResolver,
                    Settings.System.STATUSBAR_BACKGROUND_STYLE, value);
            preference.setSummary(mStatusbarBgStyle.getEntries()[index]);
            updateVisibility();
            return true;
        } else if (preference == mStatusbarBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentResolver,
                    Settings.System.STATUSBAR_BACKGROUND_COLOR, intHex);
            return true;
        } else if (preference == mNotificationBackground) {
            int index = mNotificationBackground.findIndexOfValue(newValue.toString());
            preference.setSummary(mNotificationBackground.getEntries()[index]);
            return handleBackgroundSelection(index);
        } else if (preference == mWallpaperAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(mContentResolver,
                    Settings.System.NOTIF_WALLPAPER_ALPHA, val * 0.01f);
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadable(true, false);
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putInt(mContentResolver,
                        Settings.System.NOTIF_BACKGROUND, 1);
                updateVisibility();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerListeners() {
        mNotificationBackground.setOnPreferenceChangeListener(this);
        mExpandedClockColor.setOnPreferenceChangeListener(this);
        mStatusbarBgColor.setOnPreferenceChangeListener(this);
        mStatusbarBgStyle.setOnPreferenceChangeListener(this);
        mWallpaperAlpha.setOnPreferenceChangeListener(this);
    }

    private void setDefaultValues() {
        mStatusBarNotifCount.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_NOTIF_COUNT, 0) == 1);
        mStatusBarBrightnessSlider.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_BRIGHTNESS_CONTROL, true));
        mShowImeSwitcher.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, true));
        mStatusbarBgStyle.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BACKGROUND_STYLE, 2)));
        mNotificationBackground.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.NOTIF_BACKGROUND, 2)));
        final float defaultWallpaperAlpha = Settings.System.getFloat(mContentResolver,
                Settings.System.NOTIF_WALLPAPER_ALPHA, 1f);
        mWallpaperAlpha.setInitValue((int) (defaultWallpaperAlpha * 100));
    }

    private void updateSummaries() {
        mExpandedClockColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_EXPANDED_CLOCK_COLOR, 0xFF33B5E5)));
        mStatusbarBgColor.setSummary(ColorPickerPreference.convertToARGB(Settings.System.getInt(
                mContentResolver, Settings.System.STATUSBAR_BACKGROUND_COLOR, 0xFF000000)));
        mStatusbarBgStyle.setSummary(mStatusbarBgStyle.getEntry());
        mNotificationBackground.setSummary(mNotificationBackground.getEntry());
    }

    private void updateVisibility() {
        int visible = Settings.System.getInt(mContentResolver,
                Settings.System.STATUSBAR_BACKGROUND_STYLE, 2);
        if (visible == 2) {
            mStatusbarBgColor.setEnabled(false);
        } else {
            mStatusbarBgColor.setEnabled(true);
        }

        int enabled = Settings.System.getInt(mContentResolver,
                Settings.System.NOTIF_BACKGROUND, 2);
        if (enabled == 1) {
            mWallpaperAlpha.setEnabled(true);
        } else {
            mWallpaperAlpha.setEnabled(false);
        }
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(mContentResolver,
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    private boolean handleBackgroundSelection(int index) {
        if (index == NOTIF_BACKGROUND_COLOR_FILL) {
            // Displays color dialog when user has chosen color fill
            final ColorPickerView colorView = new ColorPickerView(mActivity);
            int currentColor = Settings.System.getInt(mContentResolver,
                    Settings.System.NOTIF_BACKGROUND_COLOR, 0xFF000000);
            if (currentColor != -1) {
                colorView.setColor(currentColor);
            }
            colorView.setAlphaSliderVisible(true);
            new AlertDialog.Builder(mActivity)
            .setTitle(R.string.lockscreen_custom_background_dialog_title)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Settings.System.putInt(mContentResolver,
                            Settings.System.NOTIF_BACKGROUND_COLOR, colorView.getColor());
                    Settings.System.putInt(mContentResolver,
                            Settings.System.NOTIF_BACKGROUND, 0);
                    updateVisibility();
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
        } else if (index == NOTIF_BACKGROUND_CUSTOM_IMAGE) {
            // Used to reset the image when already set
            Settings.System.putInt(mContentResolver, Settings.System.NOTIF_BACKGROUND, 2);
            // Launches intent for user to select an image/crop it to set as background
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", false);
            intent.putExtra("scaleType", 6);
            intent.putExtra("layout_width", -1);
            intent.putExtra("layout_height", -2);
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
                wallpaperTemporary.createNewFile();
                wallpaperTemporary.setWritable(true, false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(wallpaperTemporary));
                intent.putExtra("return-data", false);
                getActivity().startActivityFromFragment(this, intent, REQUEST_PICK_WALLPAPER);
            } catch (IOException e) {
            } catch (ActivityNotFoundException e) {
            }
        } else if (index == NOTIF_BACKGROUND_DEFAULT) {
            // Sets background to default
            Settings.System.putInt(mContentResolver,
                            Settings.System.NOTIF_BACKGROUND, 2);
            updateVisibility();
            return true;
        }
        return false;
    }
}
