package com.abcmmee.tieba.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.abcmmee.tieba.R;
import com.abcmmee.tieba.database.TiebaSQLiteDao;
import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.model.User;
import com.abcmmee.tieba.utils.AlarmUtils;
import com.abcmmee.tieba.utils.BaiduTiebaUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class SignIntentService extends IntentService {

    public SignIntentService() {
        super("SignIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 设置第二天的任务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int interval = 1000 * 60 * 60 * 24;
            AlarmUtils.setServiceAlarm(this, System.currentTimeMillis() + interval);
        }

        // 执行签到
        int successCount = 0;
        int alreadyCount = 0;
        int errorCount = 0;
        List<User> mUser = TiebaSQLiteDao.getInstance(this).getAllUser();
        for (User user : mUser) {
            List<Tieba> tiebas = user.getTiebas();
            for (Tieba tieba : tiebas) {
                try {
                    BaiduTiebaUtils utils = BaiduTiebaUtils.getInstance(this);

                    int result = utils.signTiebaWithClient(user, tieba);

                    if (result == 0) {
                        alreadyCount++;
                    } else if (result == 1) {
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    errorCount++;
                }
            }
        }

        String message;
        if (alreadyCount == 0 && errorCount == 0) {
            message = "全部贴吧签到成功";
        } else {
            message = successCount + "个贴吧签到成功，" +
                    alreadyCount + "个贴吧已经签到，" +
                    errorCount + "个贴吧签到错误";
        }

        // 判断是否开启通知提醒
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNotification = preferences.getBoolean("notifications_sign_message", false);
        if (isNotification) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(message)
                    .setContentTitle("签到结果")
                    .setContentText(message)
                    .setAutoCancel(true);

            // 判断是否开启声音
            String uri = preferences.getString("notifications_sign_ringtone", "");
            if (!TextUtils.isEmpty(uri)) {
                mBuilder.setSound(Uri.parse(uri));
            }

            // 发送通知
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(0, mBuilder.build());
        }
    }

}
