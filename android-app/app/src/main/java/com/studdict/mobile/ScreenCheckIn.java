package com.studdict.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.CheckInRequest;
import com.studdict.mobile.model.CheckInResponse;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenCheckIn extends Activity {

    private long reservationId;
    private String studentId;
    private int tableNumber;
    private String dummyQr;
    private TextView statusText;
    private TextView scannedQrText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);
        studentId = getIntent().getStringExtra("STUDENT_ID");
        tableNumber = getIntent().getIntExtra("TABLE_NUMBER", -1);
        dummyQr = getIntent().getStringExtra("DUMMY_QR");

        if ((dummyQr == null || dummyQr.isBlank()) && tableNumber > 0) {
            dummyQr = "QR-" + tableNumber;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 64, 48, 48);
        root.setBackgroundColor(Color.WHITE);

        TextView back = new TextView(this);
        back.setText("<");
        back.setTextSize(42);
        back.setTextColor(Color.BLACK);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = new TextView(this);
        title.setText("Check-in");
        title.setTextSize(32);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.START);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Scan the table QR code to confirm your reservation. Use dummy mode for emulator testing.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, 12, 0, 24);
        root.addView(subtitle);

        TextView reservationText = new TextView(this);
        reservationText.setText("Reservation ID: " + reservationId);
        reservationText.setTextSize(18);
        reservationText.setTextColor(Color.DKGRAY);
        reservationText.setPadding(0, 0, 0, 16);
        root.addView(reservationText);

        scannedQrText = new TextView(this);
        scannedQrText.setText("Scanned QR: -");
        scannedQrText.setTextSize(18);
        scannedQrText.setTextColor(Color.DKGRAY);
        scannedQrText.setPadding(0, 0, 0, 24);
        root.addView(scannedQrText);

        Button checkInButton = new Button(this);
        checkInButton.setText("Scan QR Code");
        checkInButton.setAllCaps(false);
        checkInButton.setOnClickListener(v -> openQrScanner());
        root.addView(checkInButton);

        Button dummyButton = new Button(this);
        dummyButton.setText("Use Dummy QR");
        dummyButton.setAllCaps(false);
        dummyButton.setOnClickListener(v -> useDummyQr());
        root.addView(dummyButton);

        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(18);
        statusText.setTextColor(Color.DKGRAY);
        statusText.setPadding(0, 24, 0, 0);
        root.addView(statusText);

        setContentView(root);
    }

    private void useDummyQr() {
        if (dummyQr == null || dummyQr.isBlank()) {
            showStatus("No dummy QR is available for this reservation.");
            return;
        }

        scannedQrText.setText("Scanned QR: " + dummyQr);
        performCheckIn(dummyQr);
    }

    private void openQrScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the table QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                showStatus("QR scan cancelled.");
                return;
            }

            String qrData = result.getContents().trim();
            scannedQrText.setText("Scanned QR: " + qrData);
            performCheckIn(qrData);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void performCheckIn(String qrData) {
        if (reservationId <= 0 || studentId == null || studentId.isBlank()) {
            showStatus("Missing reservation or student data.");
            return;
        }

        if (qrData == null || qrData.isBlank()) {
            showStatus("No QR code was scanned.");
            return;
        }

        CheckInRequest request = new CheckInRequest(reservationId, studentId, qrData);

        ApiClient.getApi().performCheckIn(request).enqueue(new Callback<CheckInResponse>() {
            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage() != null
                            ? response.body().getMessage()
                            : "Check-in response received.";
                    showStatus(message);
                    return;
                }

                showStatus("Check-in failed. HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                showStatus("Network Error: " + t.getMessage());
            }
        });
    }

    private void showStatus(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
