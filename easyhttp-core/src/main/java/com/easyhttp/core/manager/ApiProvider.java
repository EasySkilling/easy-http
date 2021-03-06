package com.easyhttp.core.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * Name: ApiServiceProvider
 * Author: lloydfinch
 * Function: ApiServiceProvider
 * Date: 2020-09-09 14:12
 * Modify: lloydfinch 2020-09-09 14:12
 */
public class ApiProvider {
    private static HashMap<String, Object> apis;

    static {
        apis = new HashMap<>();
    }

    public static void add(String className, Object api) {
        apis.put(className, api);
    }

    /**
     * get Api by Class
     */
    public static <T> T getApi(Class<T> clazz) {
        return getApi(clazz.getCanonicalName());
    }

    /**
     * get Api by Class name
     */
    public static <T> T getApi(String clazzName) {
        Object obj = apis.get(clazzName);
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }

    /**
     * rm Api by Class
     */
    public static void rmApi(Class<?> clazz) {
        rmApi(clazz.getCanonicalName());
    }

    /**
     * rm Api by Class name
     */
    public static void rmApi(String clazzName) {
        apis.remove(clazzName);
    }
}
