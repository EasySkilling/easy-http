package com.easyhttp.dep.entity;

import com.easyhttp.dep.utils.RegexUtils;
import com.easyhttp.dep.utils.StringUtils;

public class Domain {
    /**
     * 代表域名环境对应的名称
     */
    private String envName;
    /**
     * 域名的url
     */
    private String baseUrl;

    public Domain(String envName, String baseUrl) {
        this.envName = envName;
        this.baseUrl = baseUrl;
        if (StringUtils.isEmpty(envName)) {
            throw new RuntimeException("域名环境名称envName不能为空");
        }
        if (StringUtils.isEmpty(baseUrl)) {
            throw new RuntimeException("域名地址baseUrl不能为空");
        }
        if (!RegexUtils.isValidUrl(baseUrl)) {
            throw new RuntimeException(baseUrl + "不是一个合法且有效的域名地址");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getEnvName() {
        return envName;
    }
}
