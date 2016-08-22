package com.abcmmee.tieba.ui.fragment;

import android.annotation.TargetApi;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.ui.preference.TimePreference;
import com.abcmmee.tieba.utils.AlarmUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingFragment extends PreferenceFragment {

    // "签到时间"选项
    private TimePreference mTimePreference;
    private Preference.OnPreferenceChangeListener mTimeListener;

    // "自动签到"开关
    private SwitchPreference mSwitchPreference;
    private Preference.OnPreferenceChangeListener mSwitchListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);
        setHasOptionsMenu(true);

        // "签到时间"选项
        mTimePreference = (TimePreference) findPreference("time_sign");
        mTimeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String time = (String) newValue;
                preference.setSummary(time);

                int hour = Integer.parseInt(time.split(":")[0]);
                int minute = Integer.parseInt(time.split(":")[1]);

                // 设置签到任务
                AlarmUtils.setServiceAlarm(getActivity(), hour, minute);
                return true;
            }
        };

        // "自动签到"开关
        mSwitchPreference = (SwitchPreference) findPreference("auto_sign");
        mSwitchListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean boolValue = (boolean) newValue;

                if (boolValue) {
                    // 当"自动签到"开关打开后，设置"签到时间"选项的监听,并且刚绑定的时候就执行一次
                    mTimePreference.setOnPreferenceChangeListener(mTimeListener);
                    mTimeListener.onPreferenceChange(mTimePreference, PreferenceManager
                            .getDefaultSharedPreferences(getActivity())
                            .getString(mTimePreference.getKey(), "00:15"));
                } else {
                    AlarmUtils.cancelAlarmService(getActivity());
                }
                return true;
            }
        };
        mSwitchPreference.setOnPreferenceChangeListener(mSwitchListener);

        // 一开始进入设置界面时执行一次
        mSwitchListener.onPreferenceChange(mSwitchPreference, PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getBoolean(mSwitchPreference.getKey(), false));


        // 更新通知铃声的时候，同时更改Summary的文字
        bindPreferenceSummaryToValue(findPreference("notifications_sign_ringtone"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // -----------以下代码是Android Studio创建SettingActivity时生成的模板--------------


    /**
     * Binds a preference's summary to its value.
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
}
