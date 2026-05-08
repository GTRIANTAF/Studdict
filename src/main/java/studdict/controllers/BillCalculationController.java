package studdict.controllers;

import studdict.core.Bill;
import studdict.core.Order;

public class BillCalculationController {
    public Bill startCheckout(String tableId) {
        Order order = new Order();
        double total = order.getOrderTotalforTable(tableId);

        Bill newBill = new Bill();
        newBill.createBill(total);

        return newBill;
    }
}
