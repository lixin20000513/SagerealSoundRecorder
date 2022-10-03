package com.sagereal.soundrecorder.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.constant.Constants;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //绑定设置界面,由于PreferenceActivity已被弃用,因此使用Fragment处理设置界面信息
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_frame, new SettingsFragment()).commit();
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.settings));
            bar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SwitchPreference handsetMode;
        private SwitchPreference promptRename;
        private ListPreference recordingFormat;
        private PreferenceScreen versionName;
        private SharedPreferences sharedPreferences;
        private Activity mActivity;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.setting_items, rootKey);
            initView();
            getInfo();
        }

        private void initView() {
            handsetMode = findPreference(getString(R.string.earpiece_mode));
            promptRename = findPreference(getString(R.string.prompt_rename));
            recordingFormat = findPreference(getString(R.string.recording_format));
            versionName = findPreference(getString(R.string.version_name));
            mActivity = requireActivity();
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        }

        private void getInfo() {
            if (handsetMode != null) {
                boolean isHandset = sharedPreferences.getBoolean(getString(R.string.earpiece_mode), false);
                handsetMode.setChecked(isHandset);
            }
            if (promptRename != null) {
                boolean isPromptRename = sharedPreferences.getBoolean(getString(R.string.prompt_rename), false);
                promptRename.setChecked(isPromptRename);
            }
            if (recordingFormat != null) {
                String currentFormat = sharedPreferences.getString(
                        getString(R.string.recording_format), Constants.DEFAULT_STRING);
                recordingFormat.setValue(currentFormat);
                recordingFormat.setSummary(currentFormat);
                recordingFormat.setOnPreferenceChangeListener((preference, newValue) -> {
                    String format = newValue.toString();
                    recordingFormat.setSummary(format);
                    recordingFormat.setValue(format);
                    return false;
                });
            }
            if (versionName != null) {

                String packageName = mActivity.getPackageName();
                String currentVersion;
                PackageManager manager = mActivity.getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(packageName, 0);
                    versionName.setSummary(info.versionName);
                } catch (Exception e) {
                    e.printStackTrace();
                    versionName.setSummary(getString(R.string.get_version_name_fail));
                }
            }
        }
    }
}
