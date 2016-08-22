package com.abcmmee.tieba.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Encoder {
    public static String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                int number = result[i] & 0xff;
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    sb.append(0);
                    sb.append(str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
