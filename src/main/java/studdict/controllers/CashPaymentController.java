package studdict.controllers;

import studdict.core.Table;
import studdict.core.Payment;

public class CashPaymentController {
    public boolean processCashPayment(Table table, double billAmount, double amountReceived) {
        if (amountReceived >= billAmount) {
            Payment payment = new Payment();
            payment.registerPayment();

            table.setStatus("Available");

            printReceipt(billAmount, amountReceived);

            return true;
        } else{
            System.out.println("Invalid amount");
            return false;
        }
    }

    private void printReceipt(double amount, double received ) {
        double change = received - amount;
        System.out.printf("%.2f", change);
    }
}
