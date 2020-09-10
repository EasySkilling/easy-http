package com.easyhttp.dep.interfaces;

import com.easyhttp.dep.entity.Domain;

import java.util.List;

public interface MultipleDomainSupport {
    String initDomainsMethodName = "initDomains";
    String switchDomainMethodName = "switchDomain";
    static String[] getMethodArray() {
        return new String[]{
                initDomainsMethodName,
                switchDomainMethodName
        };
    }
    List<Domain> initDomains();
    void switchDomain(String domainEnvName) throws RuntimeException;
}
