package com.easyhttp.core.entity;

import com.easyhttp.dep.enums.BodyForm;

public class ExecuteParams {
    private String baseUrl;
    private String restUrl;
    private String httpMethod;
    private boolean urlEncode;
    private BodyForm bodyForm;

    public ExecuteParams(String baseUrl, String restUrl, String httpMethod, boolean urlEncode, BodyForm bodyForm) {
        this.baseUrl = baseUrl;
        this.restUrl = restUrl;
        this.httpMethod = httpMethod;
        this.urlEncode = urlEncode;
        this.bodyForm = bodyForm;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRestUrl() {
        return restUrl;
    }

    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public boolean isUrlEncode() {
        return urlEncode;
    }

    public void setUrlEncode(boolean urlEncode) {
        this.urlEncode = urlEncode;
    }

    public BodyForm getBodyForm() {
        return bodyForm;
    }

    public void setBodyForm(BodyForm bodyForm) {
        this.bodyForm = bodyForm;
    }
}
