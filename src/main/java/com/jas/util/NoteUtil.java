package com.jas.util;

public class NoteUtil {

    
    public static boolean isNullOrEmpty(String string) {
        return (string == null) || (string.trim().length() == 0);
    }

    public static boolean isNullOrEmptyOrZero(String string) {
        return (isNullOrEmpty(string) || "0".equals(string));
    }

    public static boolean isBlankOrEmpty(String code) {
        return code != null && code.trim().length() == 0;
    }
    
}
