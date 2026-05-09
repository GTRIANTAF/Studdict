package studdict.core;


public class Bill {
    private double totalAmount;
    private double splitAmount;
    private double finalAmount;
    private String billId;
    private String reservationId;

    public Bill(String billId, String reservationId, double totalAmount) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.totalAmount = totalAmount;
        this.splitAmount = totalAmount;
        this.finalAmount = totalAmount;
    }

    public static Bill createBill(String billId, String reservationId, double totalAmount) {
        return new Bill(billId, reservationId, totalAmount);
    }

    public double calculateSplitAmount(int numOfPeople) {
        if (numOfPeople <= 0){
            throw new IllegalArgumentException("Number of people must be greater than 0.");
        }
        this.splitAmount = totalAmount / numOfPeople;
        return this.splitAmount;
    }

    public void applyDiscount(double discount) {
        if(discount < 0||discount > this.totalAmount){
            throw new IllegalArgumentException("Invalid discount value.");
        }
        this.finalAmount = this.totalAmount - discount;
    }

    public String getBillId() {
        return billId;
    }
    public String getReservationId() {
        return reservationId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public double getSplitAmount() {
        return splitAmount;
    }
    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {this.finalAmount = finalAmount;}
    public void setSplitAmount(double splitAmount) {this.splitAmount = splitAmount;}

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" +billId +'\'' +
                ", reservationId=" + reservationId +'\'' +
                ", totalAmount=" + totalAmount +
                ", splitAmount=" + splitAmount +
                ", finalAmount=" + finalAmount +
                '}';
    }
}
