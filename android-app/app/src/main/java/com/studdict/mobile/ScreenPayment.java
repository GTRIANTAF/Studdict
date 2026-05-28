package com.studdict.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.api.StuddictApi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenPayment extends AppCompatActivity {

    private TextView tvTotalAmount;
    private MaterialButton btnDigitalPayment;
    private MaterialButton btnCashPayment;

    private double totalAmount;
    private long billId;

    // Μεταβλητές για την Αυτόματη Λήξη Χρόνου (UC6 Timeout Flow)
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private final long TIMEOUT_MS = 5 * 60 * 1000; // 5 λεπτά

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_payment);

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnDigitalPayment = findViewById(R.id.btnDigitalPayment);
        btnCashPayment = findViewById(R.id.btnCashPayment);

        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 12.50);
        billId = getIntent().getLongExtra("BILL_ID", 1);

        tvTotalAmount.setText(String.format("%.2f €", totalAmount));

        // Εκκίνηση του μετρητή χρόνου με το άνοιγμα της οθόνης
        startTimeoutTimer();

        btnDigitalPayment.setOnClickListener(v -> {
            cancelTimeoutTimer();
            executeDigitalPayment();
        });

        btnCashPayment.setOnClickListener(v -> {
            cancelTimeoutTimer();
            executeCashPayment();
        });
    }

    private void startTimeoutTimer() {
        timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutRunnable = () -> {
            // Self-Call: onTimeoutExpired
            Toast.makeText(this, "Ο χρόνος αναμονής έληξε. Η συναλλαγή ακυρώθηκε.", Toast.LENGTH_LONG).show();
            finish();
        };
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);
    }

    private void cancelTimeoutTimer() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    private void executeDigitalPayment() {
        // UI Feedback κατά την αναμονή
        btnDigitalPayment.setEnabled(false);
        btnDigitalPayment.setText("Επεξεργασία...");

        StuddictApi api = ApiClient.getRetrofitInstance().create(StuddictApi.class);

        // Κλήση στο backend (Προσαρμοσμένο στο UC6 processPayment)
        Call<ResponseBody> call = api.processPayment(billId, "CARD", totalAmount);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Επιτυχής Πληρωμή -> Μετάβαση στην ScreenSuccess
                    Intent intent = new Intent(ScreenPayment.this, ScreenSuccess.class);
                    intent.putExtra("BILL_ID", billId);
                    startActivity(intent);
                    finish();
                } else {
                    // Αποτυχία (π.χ. Insufficient Balance)
                    Toast.makeText(ScreenPayment.this, "Αποτυχία πληρωμής. Παρακαλώ δοκιμάστε ξανά.", Toast.LENGTH_LONG).show();
                    btnDigitalPayment.setEnabled(true);
                    btnDigitalPayment.setText("Ψηφιακή Πληρωμή");
                    startTimeoutTimer(); // Επανεκκίνηση του χρόνου
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ScreenPayment.this, "Σφάλμα σύνδεσης με το διακομιστή.", Toast.LENGTH_LONG).show();
                btnDigitalPayment.setEnabled(true);
                btnDigitalPayment.setText("Ψηφιακή Πληρωμή");
                startTimeoutTimer();
            }
        });
    }

    private void executeCashPayment() {
        Toast.makeText(this, "Παρακαλώ πληρώστε " + String.format("%.2f €", totalAmount) + " στο ταμείο", Toast.LENGTH_LONG).show();
        // Επιστροφή στην αρχική οθόνη καθώς αναλαμβάνει το CashierUI
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimeoutTimer(); // Αποφυγή memory leaks
    }
}