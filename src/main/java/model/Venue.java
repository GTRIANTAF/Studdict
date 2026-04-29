package model;

import java.util.*;

public class Venue {
    private Long venueId;
    private String name;
    private String address;
    private String type;       // π.χ "library" | "cafe"
    private boolean isActive;
    private List<Table> tables;
}
