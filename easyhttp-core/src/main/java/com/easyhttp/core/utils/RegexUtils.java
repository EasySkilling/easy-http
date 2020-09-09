package com.easyhttp.core.utils;

public class RegexUtils {

    // 该方法正则有问题
    public static boolean isValidUrl(String url) {
        String reg = "/http(s)?:\\/\\/[\\w.]+[\\w\\/]*[\\w.]*\\??[\\w=&\\+\\%]*/is";
        return url != null && url.matches(reg);
    }
}
