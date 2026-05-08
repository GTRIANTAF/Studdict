package studdict.controllers;

import studdict.core.EBookLicense;
import studdict.core.Order;

public class TimeoutManagementController {
    public void handleTimeout(String tableId) {
        EBookLicense ebook = new EBookLicense();
        ebook.revokeLicense();

        Order order = new Order();
        order.disableFoodAndBeverage();
        BillCalculationController billCalc = new BillCalculationController();
        billCalc.startCheckout(tableId);
    }
}
