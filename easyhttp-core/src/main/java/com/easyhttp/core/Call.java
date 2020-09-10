package com.easyhttp.core;

import com.easyhttp.core.entity.Error;
import com.easyhttp.core.listener.ResultListener;

import com.easyhttp.dep.utils.GsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Call<T> {
    private okhttp3.Call call;
    private Type type;

    public Call() {

    }

    public Call<T> setType(Type type) {
        this.type = type;
        return this;
    }

    final Call<T> newOkHttpCall(okhttp3.Call call) {
        this.call = call;
        return this;
    }

    public final T sync() {
        try {
            Response response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final void async(final ResultListener<T> listener) {
        System.out.println("call type = " + type.toString());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                // TODO 具体的异常需要整理一下
                if (listener != null) {
                    listener.onError(new Error(e.getMessage()), e);
                }
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                // TODO 具体的异常需要整理一下
                int code = response.code();
                if (!response.isSuccessful()) {
                    if (listener != null) {
                        listener.onError(new Error(code, "请求数据失败"), new Exception("请求数据失败"));
                    }
                    return;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    if (listener != null) {
                        listener.onError(new Error(code, "请求数据失败"), new Exception("请求数据失败"));
                    }
                } else {
                    String json = body.string();
                    // 泛型解析数据，并且将错误数据和正确数据分别返回
                    if (listener != null) {
                        if (type == null) {
                            listener.onError(new Error("获取Listener<T>泛型参数失败!"), new Exception("获取Listener<T>泛型参数失败!"));
                            return;
                        }
                        System.out.println(json);
                        T result = GsonParser.parseToBean(json, type);
                        if (result != null) {
                            listener.onSuccess(result);
                        } else {
                            listener.onError(new Error("解析返回结果失败!"), new Exception("解析返回结果失败!"));
                        }
                    }
                }
            }
        });
    }

}
