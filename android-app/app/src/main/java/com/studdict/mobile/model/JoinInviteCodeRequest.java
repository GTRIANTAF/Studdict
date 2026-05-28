package com.studdict.mobile.model;

public class JoinInviteCodeRequest {

    private String code;
    private String guestId;

    public JoinInviteCodeRequest(String code, String guestId) {
        this.code = code;
        this.guestId = guestId;
    }

    public String getCode() {
        return code;
    }

    public String getGuestId() {
        return guestId;
    }
}