package model;

import java.util.*;

public class Venue {
    private Long venueId;
    private String name;
    private String address;
    private String type;       // π.χ "library" | "cafe"
    private boolean isActive;
    private List<Table> tables;

    public Venue() {}

    public Venue(String name, String address, String type,
                 Integer capacity, Boolean isActive) {
        this.name = name;
        this.address = address;
        this.type = type;
        this.isActive = isActive;
    }

    // Getters
    public Long getvenueId() {
        return venueId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    // Setters
    public void setVenueIdId(Long id) {
        this.venueId = venueId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
