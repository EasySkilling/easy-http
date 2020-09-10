package com.easyhttp.compiler.entity;

import com.easyhttp.compiler.enums.SupportAnnotation;
import com.squareup.javapoet.AnnotationSpec;

import java.util.Map;

public class AnnotationSpecWrapper {
    private AnnotationSpec spec;
    private SupportAnnotation annotation;
    private Map<String, Object> annotationAttrs;
    private boolean isMapClazz = false;

    public AnnotationSpecWrapper(AnnotationSpec spec, SupportAnnotation annotation, Map<String, Object> annotationAttrs) {
        this.spec = spec;
        this.annotation = annotation;
        this.annotationAttrs = annotationAttrs;
    }

    public AnnotationSpec getSpec() {
        return spec;
    }

    public void setSpec(AnnotationSpec spec) {
        this.spec = spec;
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

    public void setMapClazz(boolean mapClazz) {
        isMapClazz = mapClazz;
    }

    public boolean isMapClazz() {
        return isMapClazz;
    }
}
