package com.studdict.mobile.model;

public class ValidateInviteCodeRequest {

    private String code;

    public ValidateInviteCodeRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}