package com.easyhttp.dep.utils;

public class RegexUtils {

    public static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";


    // 该方法正则有问题
    // public static boolean isValidUrl(String url) {
    //     String reg = "/http(s)?:\\/\\/[\\w.]+[\\w\\/]*[\\w.]*\\??[\\w=&\\+\\%]*/is";
    //     return url != null && url.matches(reg);
    // }

    /**
     * Check the URL is valid
     *
     * @param url url(http/https/ftp and so on)
     * @return true if the URL is valid, other false
     */
    public static boolean isValidUrl(String url) {
        if (url == null) return false;
        String regex = URL_REGEX;
        return url.matches(regex);
    }
}
