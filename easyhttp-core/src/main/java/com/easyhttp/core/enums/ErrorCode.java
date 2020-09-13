package com.easyhttp.core.enums;

public enum ErrorCode {

    PARSE_GSON_FAILED(908);

    ErrorCode(int code) {
        this.code = code;
    }

    private int code;

}
