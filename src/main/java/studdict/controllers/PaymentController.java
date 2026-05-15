package studdict.controllers;

import studdict.core.Payment;

public class PaymentController {
    public boolean processTransaction(double amount, String paymentMethod) {
        return true;
    }

    public void finalizeCheckcout(Table table, LoyaltyWallet wallet, int points) {
        Payment payment = new Payment();
        payment.recordPayment();

        wallet.creditPoint(points);
        table.setStatus("Available");
    }
}
