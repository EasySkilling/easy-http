package com.easyhttp.core.entity;

import java.lang.reflect.Field;

public class InjectAttrs {
    private String fieldName;
    private Class<?> fieldType;
    private Field field;

    public InjectAttrs() {
    }

    public InjectAttrs(String fieldName, Class<?> fieldType, Field field) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
