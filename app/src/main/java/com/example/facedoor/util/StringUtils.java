package com.example.facedoor.util;

/**
 * Created by fyc on 2017/8/31.
 */

public class StringUtils {
    public static boolean isEmpty(String str) {
        if (str == null
                || str.length() == 0
                || str.equalsIgnoreCase("null")
                || str.isEmpty()
                || str.trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }
}
