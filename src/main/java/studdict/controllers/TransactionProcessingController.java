package studdict.controllers;

import studdict.core.Payment;

public class TransactionProcessingController {
    public void processDigitalPayment(double amount, String cardDetails) {
        boolean isApproved = sendToPaymentGatewayApi(cardDetails, amount);

        if (isApproved) {
            Payment payment = new Payment();
            payment.registerPayment();

            CompletionAndRewardController completion = new CompletionAndRewardController();
            completion.finalizeCheckout();
        } else {
            FailureManagementController failureController = new FailureManagement();
            failureController.handleFailure();
        }
    }

    private boolean sendToPaymentGatewayApi(String cardDetails, double amount) {
        return true;
    }
}
