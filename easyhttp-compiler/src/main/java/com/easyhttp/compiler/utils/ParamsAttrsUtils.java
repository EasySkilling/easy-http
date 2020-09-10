package com.easyhttp.compiler.utils;

import com.easyhttp.compiler.entity.ParamAttrsBox;
import com.easyhttp.dep.annotations.params.BodyField;
import com.easyhttp.dep.annotations.params.UrlField;
import com.easyhttp.compiler.enums.SupportAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class ParamsAttrsUtils {
    public static List<ParamAttrsBox> getPathFields(List<ParamAttrsBox> paramAttrsBoxList, Messager messager) {
        List<ParamAttrsBox> list = findSupportAnnotationParams(paramAttrsBoxList, SupportAnnotation.P_PATH_FIELD);
        // 每一个方法PathField注解url参数时，key值不能够有重复值，否则url无法正确解析
        if (list.size() > 0) {
            Map<String, ParamAttrsBox> ruleMap = new HashMap<>();
            for (ParamAttrsBox box : list) {
                String key = (String) box.getAnnotationAttrs().get("key");
                if (ruleMap.containsKey(key)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "使用@PathField注解的参数的key值，在同一个方法内部必须保持唯一！");
                }
                ruleMap.put(key, box);
            }
        }
        return list;
    }

    public static List<ParamAttrsBox> getUrlFields(List<ParamAttrsBox> paramAttrsBoxList, Messager messager) {
        List<ParamAttrsBox> list = findSupportAnnotationParams(paramAttrsBoxList, SupportAnnotation.P_URL_FIELD);
        // 每一个方法UrlField注解字段参数时，key值不能够有重复值，否则提交之后服务器无法正确解析
        checkUniqueKey(list, messager, UrlField.class);
        return list;
    }

    public static List<ParamAttrsBox> getUrlMaps(List<ParamAttrsBox> paramAttrsBoxList) {
        return findSupportAnnotationParams(paramAttrsBoxList, SupportAnnotation.P_URL_MAP);
    }

    public static List<ParamAttrsBox> getBodyFields(List<ParamAttrsBox> paramAttrsBoxList, Messager messager) {
        List<ParamAttrsBox> list = findSupportAnnotationParams(paramAttrsBoxList, SupportAnnotation.P_BODY_FIELD);
        // 每一个方法BodyField注解字段参数时，key值不能够有重复值，否则提交之后服务器无法正确解析
        checkUniqueKey(list, messager, BodyField.class);
        return list;
    }

    public static List<ParamAttrsBox> getBodyMaps(List<ParamAttrsBox> paramAttrsBoxList) {
        return findSupportAnnotationParams(paramAttrsBoxList, SupportAnnotation.P_BODY_MAP);
    }

    private static List<ParamAttrsBox> findSupportAnnotationParams(List<ParamAttrsBox> paramAttrsBoxList, SupportAnnotation target) {
        List<ParamAttrsBox> list = new ArrayList<>();
        for (ParamAttrsBox box : paramAttrsBoxList) {
            if (box.getAnnotation() == target) {
                list.add(box);
            }
        }
        return list;
    }

    private static void checkUniqueKey(List<ParamAttrsBox> list, Messager messager, Class<?> annotationClazz) {
        // 每一个方法PathField注解url参数时，key值不能够有重复值，否则url无法正确解析
        if (list.size() > 0) {
            Map<String, ParamAttrsBox> ruleMap = new HashMap<>();
            for (ParamAttrsBox box : list) {
                String key = (String) box.getAnnotationAttrs().get("key");
                if (ruleMap.containsKey(key)) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "使用@" + annotationClazz.getSimpleName() + "注解的参数的key值，在同一个方法内部必须保持唯一！");
                }
                ruleMap.put(key, box);
            }
        }
    }
}
