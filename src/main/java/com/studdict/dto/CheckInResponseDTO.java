package com.studdict.dto;

public class CheckInResponseDTO {

    private boolean successful;
    private String status;
    private String message;
    private Long checkInId;

    public CheckInResponseDTO() {
    }

    public CheckInResponseDTO(boolean successful, String status, String message, Long checkInId) {
        this.successful = successful;
        this.status = status;
        this.message = message;
        this.checkInId = checkInId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }
}
