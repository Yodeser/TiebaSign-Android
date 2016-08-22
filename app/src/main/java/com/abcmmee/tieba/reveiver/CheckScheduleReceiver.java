package com.abcmmee.tieba.reveiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.abcmmee.tieba.utils.AlarmUtils;

/**
 * 开机时检查有么有打开自动签到
 */
public class CheckScheduleReceiver extends BroadcastReceiver {
    public CheckScheduleReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoSign = sp.getBoolean("auto_sign", false);

        //如果之前打开了自动签到，就重新设置签到任务
        if (isAutoSign) {
            String time = sp.getString("time_sign", "00:15");
            int hour = Integer.parseInt(time.split(":")[0]);
            int minute = Integer.parseInt(time.split(":")[1]);

            // 设置签到任务
            AlarmUtils.setServiceAlarm(context, hour, minute);
        }
    }
}
