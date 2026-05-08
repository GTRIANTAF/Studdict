package studdict.controllers;

import studdict.core.Bill;

public class FailureManagementController {
    public void handlePaymentFailure(Bill bill) {
        System.out.println("Payment failed");
        System.out.println("Try with another method or ask the staff");

        returnToBillSreen(bill);
    }

    public void returnToBillSreen(Bill bill) {
        System.out.println("Returning to Bill Screen");
        System.out.println("Amount left to pay"_ bill.getAmount());
    }
}

