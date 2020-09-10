package com.easyhttp.demo.api;

import com.easyhttp.core.Call;
import com.easyhttp.dep.annotations.Api;
import com.easyhttp.dep.annotations.methods.Get;
import com.easyhttp.dep.annotations.params.UrlField;
import com.easyhttp.demo.entity.Result;
import com.easyhttp.demo.entity.DietPlan;

import java.util.List;

@Api(baseUrl = "http://192.168.1.6:9010/diet")
public interface ApiService {
    @Get(url = "list")
    Call<Result<List<DietPlan>>> dietPlans(@UrlField(key = "id") String id);
}
