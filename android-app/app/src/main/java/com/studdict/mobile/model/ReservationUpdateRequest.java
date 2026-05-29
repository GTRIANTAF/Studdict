package com.studdict.mobile.model;

public class ReservationUpdateRequest {
    private String newTime;
    private int newDuration;

    public ReservationUpdateRequest(String newTime, int newDuration) {
        this.newTime = newTime;
        this.newDuration = newDuration;
    }

    public String getNewTime() { return newTime; }
    public void setNewTime(String newTime) { this.newTime = newTime; }
    public int getNewDuration() { return newDuration; }
    public void setNewDuration(int newDuration) { this.newDuration = newDuration; }
}
