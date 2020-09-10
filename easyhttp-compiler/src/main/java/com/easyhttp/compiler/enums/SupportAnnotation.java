package com.easyhttp.compiler.enums;

import com.easyhttp.dep.annotations.params.BodyField;
import com.easyhttp.dep.annotations.params.BodyMap;
import com.easyhttp.dep.annotations.params.UrlField;
import com.easyhttp.dep.annotations.params.UrlMap;
import com.easyhttp.dep.annotations.paths.PathField;

public enum SupportAnnotation {
    P_URL_FIELD(UrlField.class.getCanonicalName()),
    P_URL_MAP(UrlMap.class.getCanonicalName()),
    P_BODY_FIELD(BodyField.class.getCanonicalName()),
    P_BODY_MAP(BodyMap.class.getCanonicalName()),
    P_PATH_FIELD(PathField.class.getCanonicalName()),
    M_GET("GET"),
    M_POST("POST"),
    M_DELETE("DELETE"),
    M_PUT("PUT"),
    M_PATCH("PATCH");
    SupportAnnotation(String pName) {
        this.pName = pName;
    }

    private String pName;

    public String getPName() {
        return pName;
    }
}
