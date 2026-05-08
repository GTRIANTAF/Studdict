package studdict.controllers;

import studdict.core.Reservation;
import studdict.core.Table;

public class ReservationUpdateController {
    public void updateReservation (Reservation r, Table t, int newSize, String newTime){
        r.saveChanges(newSize, newTime);

        t.reconnectQR(r);
    }
}
