package com.easyhttp.core.enums;

import com.easyhttp.core.annotations.params.BodyField;
import com.easyhttp.core.annotations.params.BodyMap;
import com.easyhttp.core.annotations.params.UrlField;
import com.easyhttp.core.annotations.params.UrlMap;
import com.easyhttp.core.annotations.paths.PathField;

public enum SupportAnnotation {
    P_URL_FIELD(UrlField.class.getCanonicalName()),
    P_URL_MAP(UrlMap.class.getCanonicalName()),
    P_BODY_FIELD(BodyField.class.getCanonicalName()),
    P_BODY_MAP(BodyMap.class.getCanonicalName()),
    P_PATH_FIELD(PathField.class.getCanonicalName()),
    M_GET("GET"),
    M_POST("post"),
    M_DELETE("delete"),
    M_PUT("put"),
    M_PATCH("patch");
    SupportAnnotation(String pName) {
        this.pName = pName;
    }

    private String pName;

    public String getPName() {
        return pName;
    }
}
