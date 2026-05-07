package model;

import jakarta.persistence.*;

@Entity
@Table(name = "private_reservations")
public class PrivateReservation extends Reservation {

    private String linkedQrCode; // Connects to the table's QR for check-in

    public PrivateReservation() {
        this.visibility = "Private";
    }

    public String getLinkedQrCode() { return linkedQrCode; }
    public void setLinkedQrCode(String linkedQrCode) { this.linkedQrCode = linkedQrCode; }
}