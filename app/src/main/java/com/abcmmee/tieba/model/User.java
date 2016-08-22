package com.abcmmee.tieba.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    private String mUid; // 百度id
    private String mName; // 百度账号名
    private String mBduss; // 登录时获取的BDUSS
    private List<Tieba> mTiebas; // 该账户所有已关注的贴吧

    public User(String uid, String name, String bduss) {
        mUid = uid;
        mName = name;
        mBduss = bduss;
        mTiebas = new ArrayList<>();
    }

    protected User(Parcel in) {
        mUid = in.readString();
        mName = in.readString();
        mBduss = in.readString();
        mTiebas = in.createTypedArrayList(Tieba.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUid);
        dest.writeString(mName);
        dest.writeString(mBduss);
        dest.writeTypedList(mTiebas);
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getBduss() {
        return mBduss;
    }

    public void setBduss(String bduss) {
        mBduss = bduss;
    }

    public List<Tieba> getTiebas() {
        return mTiebas;
    }

    public void setTiebas(List<Tieba> tiebas) {
        mTiebas = tiebas;
    }
}
