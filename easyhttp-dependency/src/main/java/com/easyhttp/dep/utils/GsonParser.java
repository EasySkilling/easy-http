package com.easyhttp.dep.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Name: GsonParser
 * Author: lloydfinch
 * Function: GsonParser
 * Date: 2020-05-20 15:19
 * Modify: lloydfinch 2020-05-20 15:19
 */
public class GsonParser {
    private static Gson sGson;
    private static final String TAG = "GsonParser";

    public static Gson getGson() {
        if (sGson == null) {
            sGson = new GsonBuilder().disableHtmlEscaping().create();
        }
        return sGson;
    }

    /**
     * parse Object to json string
     */
    public static String createJson(Object object) {
        try {
            String json = getGson().toJson(object);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * parse json string to class
     */
    public static <T> T parseToBean(String json, Class<T> clz) {
        try {
            T t = getGson().fromJson(json, clz);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T parseToBean(String json, Type type) {
        try {
            T t = getGson().fromJson(json, type);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * parse json to class list
     */
    public static <T> ArrayList<T> parseToList(String json, Class<T> clz) {
        try {
            Type type = new TypeToken<ArrayList<JsonObject>>() {
            }.getType();
            ArrayList<JsonObject> jsonObjList = getGson().fromJson(json, type);
            ArrayList<T> listOfT = new ArrayList<>();
            for (JsonObject jsonObj : jsonObjList) {
                listOfT.add(getGson().fromJson(jsonObj, clz));
            }

            return listOfT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * parse json to map list
     */
    public static <T> List<Map<String, T>> parseToMaps(String json) {
        try {
            List<Map<String, T>> list = getGson().fromJson(json, new TypeToken<List<Map<String, T>>>() {
            }.getType());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * parse json to map
     */
    public static <T> Map<String, T> parseToMap(String json) {
        try {
            Map<String, T> map = getGson().fromJson(json, new TypeToken<Map<String, T>>() {
            }.getType());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Map<String, Object> parseBeanToMap(T bean) {
        String json = createJson(bean);
        if (json == null) {
            return null;
        }
        return parseToMap(json);
    }

}