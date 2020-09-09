package com.easyhttp.compiler.entity;

import com.easyhttp.core.enums.SupportAnnotation;

import java.util.Map;

public class ParamAttrsBox {
    private String name;
    private SupportAnnotation annotation;
    private Map<String, Object> annotationAttrs;
    private boolean isMapClazz;

    public ParamAttrsBox(String name, SupportAnnotation annotation, Map<String, Object> annotationAttrs, boolean isMapClazz) {
        this.name = name;
        this.annotation = annotation;
        this.annotationAttrs = annotationAttrs;
        this.isMapClazz = isMapClazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SupportAnnotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(SupportAnnotation annotation) {
        this.annotation = annotation;
    }

    public Map<String, Object> getAnnotationAttrs() {
        return annotationAttrs;
    }

    public void setAnnotationAttrs(Map<String, Object> annotationAttrs) {
        this.annotationAttrs = annotationAttrs;
    }

    public boolean isMapClazz() {
        return isMapClazz;
    }

    public void setMapClazz(boolean mapClazz) {
        isMapClazz = mapClazz;
    }
}
