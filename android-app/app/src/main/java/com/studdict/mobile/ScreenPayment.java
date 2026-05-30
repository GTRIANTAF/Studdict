package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.PaymentRequest;
import com.studdict.mobile.model.PaymentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenPayment extends Activity {

    private TextView txtPaymentAmount;
    private EditText editCardNumber;
    private EditText editExpiry;
    private EditText editCvv;
    private Button btnProcessPayment;
    private Button btnCancelPayment;

    private Long billId;
    private double amountDue;
    private String studentId;
    private long reservationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        billId = getIntent().getLongExtra("BILL_ID", -1L);
        amountDue = getIntent().getDoubleExtra("AMOUNT", 0.0);
        studentId = getIntent().getStringExtra("STUDENT_ID");
        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);

        txtPaymentAmount = findViewById(R.id.txtPaymentAmount);
        editCardNumber = findViewById(R.id.editCardNumber);
        editExpiry = findViewById(R.id.editExpiry);
        editCvv = findViewById(R.id.editCvv);
        btnProcessPayment = findViewById(R.id.btnProcessPayment);
        btnCancelPayment = findViewById(R.id.btnCancelPayment);

        txtPaymentAmount.setText(String.format("Amount due: €%.2f", amountDue));

        btnProcessPayment.setOnClickListener(v -> processPayment());
        btnCancelPayment.setOnClickListener(v -> finish());
    }

    private void processPayment() {
        String card = editCardNumber.getText().toString().trim();
        if (card.isEmpty() || editExpiry.getText().toString().trim().isEmpty() || editCvv.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all card details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Alternative Flow 1: Payment Failure Simulation
        if (card.equals("0000") || card.equals("0000000000000000")) {
            new AlertDialog.Builder(this)
                    .setTitle("Payment Failed")
                    .setMessage("Your card was declined. Please try another payment method.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        btnProcessPayment.setEnabled(false);
        Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();

        PaymentRequest req = new PaymentRequest(billId, "DIGITAL_CARD", amountDue);
        ApiClient.getApi().processPayment(req).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    onPaymentSuccess();
                } else {
                    btnProcessPayment.setEnabled(true);
                    String msg = response.body() != null ? response.body().getMessage() : "Payment Failed.";
                    Toast.makeText(ScreenPayment.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                // Mock success for offline testing
                Toast.makeText(ScreenPayment.this, "Offline mode: Mocking Payment Success.", Toast.LENGTH_SHORT).show();
                onPaymentSuccess();
            }
        });
    }

    private void onPaymentSuccess() {
        // Post-payment actions: earn points and finalize checkout
        if (reservationId != -1L) {
            ApiClient.getApi().earnPoints(studentId, reservationId).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {}
                @Override
                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {}
            });
        }

        SessionManager session = new SessionManager(this);
        long checkInId = session.getCheckInId();
        if (checkInId != -1L) {
            ApiClient.getApi().notifyCheckout(checkInId).enqueue(new Callback<String>() {
                @Override public void onResponse(Call<String> call, Response<String> response) {}
                @Override public void onFailure(Call<String> call, Throwable t) {}
            });
            session.clearCheckIn();
        }

        showSuccessDialog();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Payment Successful")
                .setMessage("Your digital receipt has been issued and your table is now released. Thank you!")
                .setPositiveButton("Close", (dialog, which) -> {
                    // Return to venues/main screen
                    Intent intent = new Intent(ScreenPayment.this, ScreenVenues.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
