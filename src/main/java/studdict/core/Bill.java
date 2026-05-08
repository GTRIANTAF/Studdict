package studdict.core;

public class Bill {
    private double amount;
    private String details;

    public void createBill(double totalAmount) {
        this.amount = totalAmount;
    }
}
