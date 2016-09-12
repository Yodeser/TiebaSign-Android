package com.abcmmee.tieba.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.abcmmee.tieba.service.SignIntentService;

import java.util.Calendar;

/**
 * 这个类用来设置签到任务
 */
public class AlarmUtils {
    private static final String TAG = "AlarmUtils";

    // 使构造器私有化
    private AlarmUtils() {

    }

    /**
     * 设置签到任务
     *
     * @param context
     * @param hour
     * @param minute
     */
    public static void setServiceAlarm(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hour, minute, 0);
        long triggerAtMillis = calendar.getTimeInMillis();

        // 在这里一定要判断当前设置的时间是不是小于现在的时间，是的话要加一天
        if (triggerAtMillis < System.currentTimeMillis()) {
            triggerAtMillis += 1000 * 60 * 60 * 24;
        }

        setServiceAlarm(context, triggerAtMillis);
    }

    public static void setServiceAlarm(Context context, long triggerAtMillis) {
        Log.d(TAG, "AlarmService start");

        Intent i = new Intent(context, SignIntentService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // API19以后AlarmManager.setRepeating不一定按时执行，官方文档有说明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 参考：http://blog.csdn.net/henny_fack/article/details/51745643
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 参考：http://stackoverflow.com/questions/34074955/android-exact-alarm-is-always-3-minutes-off
            // 参考：https://stackoverflow.com/questions/33260851/alarmmanager-not-working-as-expected-in-sleep-mode-on-s5-neo
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerAtMillis, pi);
            alarmManager.setAlarmClock(alarmClockInfo, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // setExact不会重复执行，所以SignIntentService.class里面还要再设置一次
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            int interval = 1000 * 60 * 60 * 24;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, pi);
        }

    }

    /**
     * 取消签到任务
     */
    public static void cancelAlarmService(Context context) {
        Log.d(TAG, "AlarmService is cancel");
        Intent i = new Intent(context, SignIntentService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }

    /**
     * 判断签到任务是否开启
     * 失效！
     * 原因：未知
     * 状态：未解决
     */
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, SignIntentService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
}
