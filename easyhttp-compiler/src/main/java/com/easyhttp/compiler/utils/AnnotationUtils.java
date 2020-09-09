package com.easyhttp.compiler.utils;

import com.easyhttp.compiler.entity.AnnotationSpecWrapper;
import com.easyhttp.core.enums.SupportAnnotation;

import java.util.List;

public class AnnotationUtils {

    public static boolean isMoreThanOneEasyHttpParamAnnotation(List<AnnotationSpecWrapper> annotationSpecList) {
        if (annotationSpecList.size() > 1) {
            int count = 0;
            for (AnnotationSpecWrapper wrapper : annotationSpecList) {
                if (isSupportParamAnnotation(wrapper.getAnnotation())) {
                    count++;
                }
                if (count > 1) {
                    break;
                }
            }
            return count > 1;
        }
        return false;
    }

    private static boolean isSupportParamAnnotation(SupportAnnotation annotation) {
        return annotation == SupportAnnotation.P_BODY_FIELD
                || annotation == SupportAnnotation.P_BODY_MAP
                || annotation == SupportAnnotation.P_PATH_FIELD
                || annotation == SupportAnnotation.P_URL_FIELD
                || annotation == SupportAnnotation.P_URL_MAP;
    }

    public static boolean hasOnlyOneEasyHttpAnnotation(List<AnnotationSpecWrapper> annotationSpecList) {
        if (annotationSpecList.size() > 0) {
            int count = 0;
            for (AnnotationSpecWrapper wrapper : annotationSpecList) {
                if (isSupportMethodAnnotation(wrapper.getAnnotation())) {
                    count++;
                }
                if (count > 1) {
                    break;
                }
            }
            return count == 1;
        }
        return false;
    }

    public static boolean isSupportMethodAnnotation(SupportAnnotation annotation) {
        return annotation == SupportAnnotation.M_GET
                || annotation == SupportAnnotation.M_POST
                || annotation == SupportAnnotation.M_DELETE
                || annotation == SupportAnnotation.M_PUT
                || annotation == SupportAnnotation.M_PATCH;
    }

}
