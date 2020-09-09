package com.easyhttp.core.entity;

public class Error {
    private int code;
    private String err;

    public Error(String err) {
        this.err = err;
    }

    public Error(int code, String err) {
        this.code = code;
        this.err = err;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }
}
