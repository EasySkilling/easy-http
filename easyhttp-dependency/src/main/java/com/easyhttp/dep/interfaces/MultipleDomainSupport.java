package com.easyhttp.dep.interfaces;

import com.easyhttp.dep.entity.Domain;

import java.util.List;

public interface MultipleDomainSupport {
    String initDomainsMethodName = "initDomains";
    String addDomainMethodName = "addDomain";
    String switchDomainMethodName = "switchDomain";
    static String[] getMethodArray() {
        return new String[]{
                initDomainsMethodName,
                addDomainMethodName,
                switchDomainMethodName
        };
    }
    List<Domain> initDomains();
    void addDomain(Domain domain);
    void switchDomain(String domainEnvName) throws RuntimeException;
}
