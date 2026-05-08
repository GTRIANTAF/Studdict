package studdict.controllers;

import studdict.core.Bill;

public class SplitBillController {
    public void calculateSplitBill(Bill bill, int numOfPeople) {
        double share = bill.getAmount() / numOfPeople;
        sendPushNotification(share);
    }

    public void sendPushNotification(double share) {
        System.out.println("Push Notification: You owe "+ share);
    }
}
