package com.easyhttp.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.easyhttp.core.entity.Error;
import com.easyhttp.core.listener.ResultListener;
import com.easyhttp.core.thread.ThreadType;
import com.easyhttp.demo.api.ApiService;
import com.easyhttp.demo.consts.DomainConst;
import com.easyhttp.demo.entity.DietPlan;
import com.easyhttp.demo.entity.Result;
import com.easyhttp.dep.annotations.Autowired;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Autowired(singleton = true)
    private ApiService apiService;

    private String domainEnvName = DomainConst.ENV_DEV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.switchDomainBtna).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DomainConst.ENV_DEV.equals(domainEnvName)) {
                    domainEnvName = DomainConst.ENV_PRO;
                } else {
                    domainEnvName = DomainConst.ENV_DEV;
                }
                apiService.switchDomain(domainEnvName);
            }
        });
        findViewById(R.id.callBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callApi();
                    }
                }).start();

            }
        });
    }

    private void callApi() {
       apiService.dietPlans("1297442491489964034").asyncRequest(new ResultListener<Result<List<DietPlan>>>() {
           @Override
           public void onSuccess(Result<List<DietPlan>> data) {
               Log.e(TAG, "onSuccess: " + data.toString());
           }

           @Override
           public void onError(Error error, Exception e) {
               Log.e(TAG, "threadName = " + Thread.currentThread().getName());
               Log.e(TAG, "onError: " + e.getLocalizedMessage());
           }
       }, ThreadType.WORK);
        // try {
        //     Result<List<DietPlan>> result = apiService.dietPlans("2222222222").syncRequest();
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}