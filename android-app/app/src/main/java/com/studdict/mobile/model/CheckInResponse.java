package com.studdict.mobile.model;

public class CheckInResponse {

    private boolean successful;
    private String status;
    private String message;
    private Long checkInId;

    public boolean isSuccessful() {
        return successful;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Long getCheckInId() {
        return checkInId;
    }
}
