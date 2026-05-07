package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private Long venueId;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "type")
    private String type;       // π.χ "library" | "cafe"

    @Column(name = "is_active")
    private boolean isActive;

    // Defines the One-to-Many relationship with StudyTable.
    // CascadeType.ALL ensures that if you delete a Venue, its tables are deleted too.
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id") // Links the tables to this venue's ID
    private List<StudyTable> tables = new ArrayList<>();

    public Venue() {}

    public Venue(String name, String address, String type, boolean isActive) {
        this.name = name;
        this.address = address;
        this.type = type;
        this.isActive = isActive;
    }

    // Getters
    public Long getVenueId() {
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

    public boolean isActive() {
        return isActive;
    }

    public List<StudyTable> getTables() {
        return tables;
    }

    // Setters
    public void setVenueId(Long venueId) {
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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setTables(List<StudyTable> tables) {
        this.tables = tables;
    }
}