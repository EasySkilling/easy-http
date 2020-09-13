package com.easyhttp.core.exception;

public class EasyHttpException extends Exception {
    private int code;

    public EasyHttpException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
