package com.easyhttp.dep.interfaces;

import com.easyhttp.dep.entity.Domain;

import java.util.List;

public interface MultipleDomainSupport {
    List<Domain> initDomains();
    void switchDomain(String domainEnvName);
}
