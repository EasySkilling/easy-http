package com.easyhttp.demo;

import android.os.Bundle;
import android.util.Log;

import com.easyhttp.core.Call;
import com.easyhttp.core.annotations.Autowired;
import com.easyhttp.core.entity.Error;
import com.easyhttp.core.listener.ResultListener;
import com.easyhttp.core.manager.ApiProvider;
import com.easyhttp.demo.api.ApiService;
import com.easyhttp.demo.entity.DietPlan;
import com.easyhttp.demo.entity.Result;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Autowired
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        browseBaiDu();
    }

    private void browseBaiDu() {
        apiService.dietPlans("1297442491489964034").async(new ResultListener<Result<List<DietPlan>>>() {
            @Override
            public void onSuccess(Result<List<DietPlan>> data) {
                Log.e(TAG, "onSuccess: " + data.toString());
            }

            @Override
            public void onError(Error error, Exception e) {
                Log.e(TAG, "onError: " + e.getLocalizedMessage());
            }
        });

        Call<Result<List<DietPlan>>> resultCall = ApiProvider.getApi(ApiService.class).dietPlans("1297442491489964034");
    }
}
