package com.easyhttp.core.listener;

import com.easyhttp.core.entity.Error;

public interface ResultListener<T> {
    void onSuccess(T data);
    void onError(Error error, Exception e);
}
