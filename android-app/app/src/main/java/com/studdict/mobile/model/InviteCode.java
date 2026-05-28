package com.studdict.mobile.model;

public class InviteCode {

    private Long inviteCodeId;
    private String code;
    private String createdAt;
    private String expiresAt;
    private boolean active;

    public Long getInviteCodeId() {
        return inviteCodeId;
    }

    public String getCode() {
        return code;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }
}