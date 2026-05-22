package com.studdict.model;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Links Private and Public tables to this primary table
@Table(name = "reservations")
public abstract class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "number_of_people")
    private int numberOfPeople;

    @Column(name = "status") // e.g., PENDING, CONFIRMED
    private String status;

    @Column(name = "visibility") // Private or Public
    protected String visibility;

    // Relationship to the specific table selected during the booking flow[cite: 1]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private StudyTable table;

    // Relationship to all students participating in this reservation[cite: 1]
    @OneToMany(mappedBy = "reservationId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservationParticipant> participants = new ArrayList<>();

    public Reservation() {}

    // Getters
    public Long getReservationId() { return reservationId; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getNumberOfPeople() { return numberOfPeople; }
    public String getStatus() { return status; }
    public String getVisibility() { return visibility; }
    public StudyTable getTable() { return table; }
    public List<ReservationParticipant> getParticipants() { return participants; }

    // Setters
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public void setReservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public void setStatus(String status) { this.status = status; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setTable(StudyTable table) { this.table = table; }
    public void setParticipants(List<ReservationParticipant> participants) { this.participants = participants; }

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Bill bill;

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
}