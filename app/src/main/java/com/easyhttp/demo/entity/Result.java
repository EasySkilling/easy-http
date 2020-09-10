package com.easyhttp.demo.entity;

public class Result<T> {
    public int code;
    public boolean success;
    public String msg;
    public T data;

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", success=" + success +
                ", msg='" + msg + '\'' +
                ", data=" + data.toString() +
                '}';
    }
}
