package com.easyhttp.core;

import com.easyhttp.core.entity.Error;
import com.easyhttp.core.enums.ErrorCode;
import com.easyhttp.core.exception.EasyHttpException;
import com.easyhttp.core.listener.ResultListener;

import com.easyhttp.core.thread.ThreadType;
import com.easyhttp.core.thread.UiThread;
import com.easyhttp.dep.utils.GsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class Call<T> {
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

    public T syncRequest() throws Exception {
        Response response = call.execute();
        return parseResponse(response);
    }

    public void asyncRequest(final ResultListener<T> listener) {
        asyncRequest(listener, ThreadType.MAIN);
    }

    public void cancelRequest() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    public final void asyncRequest(final ResultListener<T> listener, final ThreadType threadType) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                // TODO 具体的异常需要整理一下
                callbackError(listener, threadType, new Error(e.getMessage()), e);
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
                try {
                    T result = parseResponse(response);
                    callbackResult(listener, threadType, result);
                } catch (Exception e) {
                    if (e instanceof EasyHttpException) {
                        callbackError(listener, threadType, new Error(((EasyHttpException)e).getCode(), e.getMessage()), e);
                    } else {
                        callbackError(listener, threadType, new Error(e.getMessage()), e);
                    }
                }
            }
        });
    }

    private T parseResponse(Response response) throws Exception {
        // TODO 具体的异常需要整理一下
        int code = response.code();
        if (!response.isSuccessful()) {
            throw new EasyHttpException("请求数据失败", code);
        }
        ResponseBody body = response.body();
        if (body == null) {
            throw new EasyHttpException("请求数据失败", code);
        }
        String json = body.string();
        T result = GsonParser.parseToBean(json, type);
        if (result == null) {
            throw new EasyHttpException("解析返回结果失败", ErrorCode.PARSE_GSON_FAILED.ordinal());
        }
        return result;
    }

    private void callbackError(final ResultListener<T> listener, ThreadType threadType, final Error error, final Exception e) {
        if (listener == null) {
            return;
        }
        if (ThreadType.WORK == threadType) {
            listener.onError(error, e);
        } else {
            UiThread.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onError(error, e);
                }
            });
        }
    }

    private void callbackResult(final ResultListener<T> listener, ThreadType threadType, final T result) {
        if (listener == null) {
            return;
        }
        if (ThreadType.WORK == threadType) {
            listener.onSuccess(result);
        } else {
            UiThread.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(result);
                }
            });
        }
    }

}
