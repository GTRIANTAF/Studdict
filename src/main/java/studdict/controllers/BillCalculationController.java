package studdict.controllers;

import studdict.core.Bill;
import studdict.core.Order;
import java.util.List;

public class BillCalculationController {
   public Bill startCheckout(String tableId) {
       List<Order> orderList = getOrdersForTable(tableId);

       double totalAmount = calculateTotal(orderList);

       String billId = generateBillId();

       return Bill.createBill(billId, tableId, totalAmount);
   }

   private List<Order> getOrdersForTable(String tableId) {
       return List.of();
   }

   private double calculateTotal(List<Order> orderList) {
       return orders.stream()
               .mapToDouble(Order::getSubTotal)
               .sum();
   }

   private String generateBillId() {
       return "Bill-" + System.currentTimeMillis();
   }
}
