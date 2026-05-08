package studdict.core;

public class Table {

    public boolean checkAvailability(int newSize, String newTime) {
        return true;
    }

    public void reconnectQR(Reservation reservation) {
        System.out.println("Σύνδεση με την κράτηση");
    }
    public void freeTable() {
        System.out.println("Table free");
    }
}
