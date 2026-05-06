package model;

import java.time.*;
import java.util.*;

public abstract class Reservation {
    private String reservationId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private int durationMinutes;
    private int numberOfPeople;
    private String status; // π.χ. PENDING, CONFIRMED
    private Tables tables;
    String visibility;
    private List<ReservationParticipant> participants;
}
