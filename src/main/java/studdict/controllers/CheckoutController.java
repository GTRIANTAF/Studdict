package studdict.controllers;

import studdict.core.Bill;
import studdict.core.Order;

public class CheckoutController {
    public Bill generateBill(Reservation reservation) {
        Order order = new Order();
        String orderDetails = order.getClass(reservation.getId());

        Bill newBill = new Bill();
        newBill.createBill(orderDetails);
        return newBill;
    }
}
