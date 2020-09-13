package com.easyhttp.demo.api;

import com.easyhttp.core.Call;
import com.easyhttp.demo.consts.DomainConst;
import com.easyhttp.dep.annotations.Api;
import com.easyhttp.dep.annotations.methods.Get;
import com.easyhttp.dep.annotations.params.UrlField;
import com.easyhttp.demo.entity.Result;
import com.easyhttp.demo.entity.DietPlan;
import com.easyhttp.dep.entity.Domain;
import com.easyhttp.dep.interfaces.MultipleDomainSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Api(baseUrl = "http://192.168.1.6:9010/diet")
//public interface ApiService  {
@Api
public interface ApiService extends MultipleDomainSupport {

    @Override
    default List<Domain> initDomains() {
        List<Domain> domainList = new ArrayList<>(2);
        domainList.add(new Domain(DomainConst.ENV_DEV, "http://192.168.1.6:9010"));
        domainList.add(new Domain(DomainConst.ENV_PRO, "http://www.baidu.com"));
        return domainList;
    }

    @Get(url = "list")
    Call<Result<List<DietPlan>>> dietPlans(@UrlField(key = "id") String id);
}
