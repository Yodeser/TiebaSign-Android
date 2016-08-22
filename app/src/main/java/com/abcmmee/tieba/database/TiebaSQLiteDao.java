package com.abcmmee.tieba.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.abcmmee.tieba.model.Tieba;
import com.abcmmee.tieba.model.User;

import java.util.ArrayList;
import java.util.List;


public class TiebaSQLiteDao {

    private static TiebaSQLiteDao mTiebaSQLiteDao;

    private TiebaSQLiteOpenHelper mHelper;
    private SQLiteDatabase db;

    // 私有化构造器
    private TiebaSQLiteDao(Context context) {
        mHelper = new TiebaSQLiteOpenHelper(context);
        db = mHelper.getWritableDatabase();
    }

    // 获取TiebaSQLiteDao实例
    public synchronized static TiebaSQLiteDao getInstance(Context context) {
        if (mTiebaSQLiteDao == null)
            mTiebaSQLiteDao = new TiebaSQLiteDao(context);
        return mTiebaSQLiteDao;
    }

    /**
     * 从数据库中获取所有账户
     *
     * @return
     */
    public List<User> getAllUser() {
        List<User> users = new ArrayList<>();
        Cursor cursor = mHelper.queryAllUser(); // 打开游标
        while (cursor.moveToNext()) {
            String uid = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_USER_UID));
            String name = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_USER_NAME));
            String bduss = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_USER_BDUSS));
            User user = new User(uid, name, bduss);

            // 获取该用户已关注的贴吧
            List<Tieba> tiebas = getAllTieba(user);
            user.setTiebas(tiebas);
            users.add(user);
        }
        cursor.close(); // 关闭游标
        return users;
    }

    /**
     * 从数据库中获取单个账户
     *
     * @param uid
     * @return
     */
    public User getUser(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            Cursor cursor = mHelper.queryUser(uid); // 打开游标
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_USER_NAME));
                String bduss = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_USER_BDUSS));
                User user = new User(uid, name, bduss);

                // 获取该用户已关注的贴吧
                List<Tieba> tiebas = getAllTieba(user);
                user.setTiebas(tiebas);
                return user;
            }
            cursor.close(); //关闭游标
        }
        return null;
    }

    /**
     * 往数据库保存账户
     *
     * @param user
     */
    public void addUser(User user) {
        if (user != null) {

            // 判断用户是否存在，如果存在就更新旧用户，否则就插入新用户
            Cursor cursor = mHelper.queryUser(user.getUid());
            if (cursor.moveToFirst()) {
                mHelper.updateUser(user);
            } else {
                mHelper.insertUser(user); // 往数据库插入用户
            }
            cursor.close();

            List<Tieba> tiebas = user.getTiebas();
            addAllTieba(tiebas); // 保存用户所关注的贴吧
        }
    }

    public void removeUser(User user) {
        if (user != null)
            mHelper.deleteUser(user);
    }

    /**
     * 从数据库中获取某个用户所有已关注的贴吧
     *
     * @param user
     * @return
     */
    public List<Tieba> getAllTieba(User user) {
        List<Tieba> tiebas = new ArrayList<>();
        Cursor cursor = mHelper.queryTieba(user.getUid()); // 打开游标

        while (cursor.moveToNext()) {
            // 获取贴吧数据库字段对应的值
            String fid = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_TIEBA_FID));
            String name = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_TIEBA_NAME));
            String level = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_TIEBA_LEVEL));
            String exp = cursor.getString(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_TIEBA_EXP));
            int status = cursor.getInt(cursor.getColumnIndex(TiebaSQLiteOpenHelper.COLUMN_TIEBA_STATUS));
            boolean statusBoolean = status != 0;

            // 把获取到的值赋值给Tieba对象
            Tieba tieba = new Tieba(user.getUid(), name, level, exp);
            tieba.setFid(fid);
            tieba.setStatus(statusBoolean);

            tiebas.add(tieba);
        }
        cursor.close(); // 关闭游标
        return tiebas;
    }

    /**
     * 往数据库中存入某个用户的所有已关注的贴吧
     *
     * @param tiebas
     */
    public void addAllTieba(List<Tieba> tiebas) {
        if (tiebas != null) {
            for (Tieba tieba : tiebas) {
                addTieba(tieba);
            }
        }
    }

    /**
     * 往数据库中添加一个贴吧
     *
     * @param tieba
     */
    public void addTieba(Tieba tieba) {
        if (tieba != null) {
            // 保存贴吧前，判断贴吧是否存在
            Cursor cursor = mHelper.queryTieba(tieba);
            if (cursor.moveToFirst()) {
                mHelper.updateTieba(tieba);
            } else {
                mHelper.insertTieba(tieba);
            }
            cursor.close();
        }
    }

    public void removeTieba(Tieba tieba) {
        if (tieba != null) {
            mHelper.deleteTieba(tieba);
        }
    }
}
