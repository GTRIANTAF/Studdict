package studdict.core;

public class Reservation {
    public String getDetails() {
        return "Στοιχεία κράτησης";
    }

    public void saveChanges(int newGroupSize, String newTime) {
        System.out.println("Τα νέα στοιχεία αποθηκεύτηκαν.");
    }
}
