package model;

public class Tables {
    private Venue venue;
    private int tableId;
    private int tableNumber;
    private int capacity;
    private String qrCodeString;
    private boolean isAvailable;

    public Tables() {}

    public Tables(Venue venue, Integer tableNumber, Integer capacity,
                      String qrCodeString, Boolean isAvailable) {
        this.venue = venue;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.qrCodeString = qrCodeString;
        this.isAvailable = isAvailable;
    }

    // Getters
    public int getId() {
        return tableId;
    }

    public Venue getVenue() {
        return venue;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getQrCodeString() {
        return qrCodeString;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    // Setters
    public void setId(int tableId) {
        this.tableId = tableId;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setQrCodeString(String qrCodeString) {
        this.qrCodeString = qrCodeString;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
