package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.CheckInResponse;
import com.studdict.mobile.model.ConfirmCheckInRequest;
import com.studdict.mobile.model.ReservationParticipant;
import com.studdict.mobile.model.ValidateCheckInRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenCheckIn extends Activity {

    private static final int PURPLE = Color.parseColor("#A855F7");
    private static final int PURPLE_DARK = Color.parseColor("#7E22CE");
    private static final int PURPLE_SOFT = Color.parseColor("#F3E8FF");
    private static final int TEXT = Color.parseColor("#111827");
    private static final int MUTED = Color.parseColor("#4B5563");
    private static final int BORDER = Color.parseColor("#E9D5FF");
    private static final int BACKGROUND = Color.parseColor("#FBF7FF");

    private long reservationId;
    private String studentId;
    private int tableNumber;
    private String dummyQr;
    private String currentQrData;

    private TextView statusText;
    private TextView scannedQrText;
    private LinearLayout participantsContainer;
    private Button finalizeButton;

    private final List<CheckBox> participantCheckBoxes = new ArrayList<>();

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

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 56, 40, 48);
        root.setBackgroundColor(BACKGROUND);
        scrollView.addView(root);

        TextView back = new TextView(this);
        back.setText("<");
        back.setTextSize(40);
        back.setTextColor(Color.BLACK);
        back.setTypeface(null, Typeface.BOLD);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = new TextView(this);
        title.setText("Check-in");
        title.setTextSize(34);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.START);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 10);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Scan the table QR code to confirm your reservation.\nUse dummy mode for emulator testing.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(MUTED);
        subtitle.setPadding(0, 0, 0, 24);
        root.addView(subtitle);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(28, 28, 28, 28);
        card.setBackground(roundedBackground(Color.WHITE, BORDER, 28, 2));
        root.addView(card, matchWidthWithBottomMargin(24));

        TextView reservationText = new TextView(this);
        reservationText.setText("Reservation ID: " + reservationId);
        reservationText.setTextSize(18);
        reservationText.setTextColor(TEXT);
        reservationText.setPadding(0, 0, 0, 14);
        card.addView(reservationText);

        scannedQrText = new TextView(this);
        scannedQrText.setText("Scanned QR: -");
        scannedQrText.setTextSize(18);
        scannedQrText.setTextColor(TEXT);
        scannedQrText.setPadding(0, 0, 0, 24);
        card.addView(scannedQrText);

        Button scanButton = createOutlinedButton("Scan QR Code");
        scanButton.setOnClickListener(v -> openQrScanner());
        card.addView(scanButton, matchWidthWithBottomMargin(14));

        Button dummyButton = createSoftButton("Use Dummy QR");
        dummyButton.setOnClickListener(v -> useDummyQr());
        card.addView(dummyButton, matchWidthWithBottomMargin(8));

        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(18);
        statusText.setTextColor(MUTED);
        statusText.setPadding(0, 22, 0, 10);
        card.addView(statusText);

        participantsContainer = new LinearLayout(this);
        participantsContainer.setOrientation(LinearLayout.VERTICAL);
        participantsContainer.setPadding(0, 8, 0, 16);
        card.addView(participantsContainer);

        finalizeButton = createPrimaryButton("Finalize arrival");
        finalizeButton.setVisibility(View.GONE);
        finalizeButton.setOnClickListener(v -> finalizeArrival());
        card.addView(finalizeButton, matchWidthWithBottomMargin(0));

        setContentView(scrollView);
    }

    private void useDummyQr() {
        if (dummyQr == null || dummyQr.isBlank()) {
            showStatus("No dummy QR is available for this reservation.");
            return;
        }

        handleQrData(dummyQr);
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

            handleQrData(result.getContents().trim());
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleQrData(String qrData) {
        currentQrData = qrData;
        scannedQrText.setText("Scanned QR: " + qrData);
        clearParticipants();
        finalizeButton.setVisibility(View.GONE);
        validateCheckIn(qrData);
    }

    private void validateCheckIn(String qrData) {
        if (reservationId <= 0) {
            showStatus("Missing reservation data.");
            return;
        }

        if (qrData == null || qrData.isBlank()) {
            showStatus("No QR code was scanned.");
            return;
        }

        ValidateCheckInRequest request = new ValidateCheckInRequest(reservationId, qrData);

        ApiClient.getApi().validateCheckIn(request).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showStatus("Check-in validation failed. HTTP " + response.code());
                    return;
                }

                String result = response.body();

                if ("VALID_CHECK_IN".equals(result)) {
                    showStatus("The QR is correct. Select the members who are present.");
                    loadParticipants();
                    return;
                }

                showStatus(messageForValidationResult(result));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showStatus("Network Error: " + t.getMessage());
            }
        });
    }

    private void loadParticipants() {
        ApiClient.getApi().getCheckInParticipants(reservationId).enqueue(new Callback<List<ReservationParticipant>>() {
            @Override
            public void onResponse(Call<List<ReservationParticipant>> call, Response<List<ReservationParticipant>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showStatus("Could not load reservation participants. HTTP " + response.code());
                    return;
                }

                renderParticipants(response.body());
            }

            @Override
            public void onFailure(Call<List<ReservationParticipant>> call, Throwable t) {
                showStatus("Network Error: " + t.getMessage());
            }
        });
    }

    private void renderParticipants(List<ReservationParticipant> participants) {
        clearParticipants();

        if (participants == null || participants.isEmpty()) {
            showStatus("No members were found for this reservation.");
            finalizeButton.setVisibility(View.GONE);
            return;
        }

        TextView header = new TextView(this);
        header.setText("Reservation members");
        header.setTextSize(20);
        header.setTextColor(Color.BLACK);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(0, 12, 0, 4);
        participantsContainer.addView(header);

        TextView hint = new TextView(this);
        hint.setText("Select who is present.");
        hint.setTextSize(15);
        hint.setTextColor(MUTED);
        hint.setPadding(0, 0, 0, 12);
        participantsContainer.addView(hint);

        for (ReservationParticipant participant : participants) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(labelForParticipant(participant));
            checkBox.setTextSize(18);
            checkBox.setTextColor(TEXT);
            checkBox.setPadding(12, 8, 12, 8);
            checkBox.setButtonTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{PURPLE, Color.parseColor("#CBD5E1")}
            ));
            checkBox.setTag(participant.getId());

            if (participant.isCheckedIn()) {
                checkBox.setChecked(true);
                checkBox.setEnabled(false);
                checkBox.setTextColor(MUTED);
            }

            participantsContainer.addView(checkBox);
            participantCheckBoxes.add(checkBox);
        }

        finalizeButton.setVisibility(View.VISIBLE);
    }

    private String labelForParticipant(ReservationParticipant participant) {
        String label = participant.getStudentId();

        if (participant.getRole() != null && !participant.getRole().isBlank()) {
            label += " - " + participant.getRole();
        }

        if (participant.isCheckedIn()) {
            label += " (already checked-in)";
        }

        return label;
    }

    private void finalizeArrival() {
        if (currentQrData == null || currentQrData.isBlank()) {
            showStatus("Scan the QR code first.");
            return;
        }

        List<Long> selectedParticipantIds = new ArrayList<>();

        for (CheckBox checkBox : participantCheckBoxes) {
            Object tag = checkBox.getTag();

            if (checkBox.isEnabled() && checkBox.isChecked() && tag instanceof Long) {
                selectedParticipantIds.add((Long) tag);
            }
        }

        if (selectedParticipantIds.isEmpty()) {
            showStatus("Select at least one present member.");
            return;
        }

        ConfirmCheckInRequest request = new ConfirmCheckInRequest(reservationId, currentQrData, selectedParticipantIds);

        ApiClient.getApi().confirmCheckIn(request).enqueue(new Callback<CheckInResponse>() {
            @Override
            public void onResponse(Call<CheckInResponse> call, Response<CheckInResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccessful()) {
                    openSuccessScreen(selectedParticipantIds.size());
                    return;
                }

                if (response.body() != null && response.body().getStatus() != null) {
                    showStatus(messageForConfirmStatus(response.body().getStatus()));
                    return;
                }

                showStatus("Check-in was not completed. HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<CheckInResponse> call, Throwable t) {
                showStatus("Network Error: " + t.getMessage());
            }
        });
    }

    private void openSuccessScreen(int checkedInCount) {
        Intent intent = new Intent(ScreenCheckIn.this, ScreenCheckInSuccess.class);
        intent.putExtra("RESERVATION_ID", reservationId);
        intent.putExtra("CHECKED_IN_COUNT", checkedInCount);
        intent.putExtra("STUDENT_ID", studentId);
        startActivity(intent);
        finish();
    }

    private String messageForValidationResult(String result) {
        if ("WRONG_TABLE".equals(result)) {
            return "The QR code does not match the reservation table.";
        }

        if ("WRONG_TIME".equals(result)) {
            return "The reservation is not active at this time.";
        }

        if ("RESERVATION_NOT_FOUND".equals(result)) {
            return "Reservation was not found.";
        }

        return "Check-in failed. Check the QR code or reservation details.";
    }

    private String messageForConfirmStatus(String status) {
        if ("SUCCESS".equals(status)) {
            return "Check-in completed successfully.";
        }

        if ("NO_PARTICIPANTS_SELECTED".equals(status)) {
            return "Select at least one present member.";
        }

        if ("NO_ELIGIBLE_PARTICIPANTS".equals(status)) {
            return "The selected members are already checked-in or do not belong to this reservation.";
        }

        return messageForValidationResult(status);
    }

    private void clearParticipants() {
        participantsContainer.removeAllViews();
        participantCheckBoxes.clear();
    }

    private void showStatus(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(roundedBackground(PURPLE, PURPLE, 20, 0));
        button.setPadding(16, 18, 16, 18);
        return button;
    }

    private Button createSoftButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTextColor(PURPLE_DARK);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(roundedBackground(PURPLE_SOFT, BORDER, 20, 2));
        button.setPadding(16, 18, 16, 18);
        return button;
    }

    private Button createOutlinedButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTextColor(Color.BLACK);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(roundedBackground(Color.WHITE, PURPLE, 20, 2));
        button.setPadding(16, 18, 16, 18);
        return button;
    }

    private GradientDrawable roundedBackground(int fillColor, int strokeColor, int radiusDp, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private LinearLayout.LayoutParams matchWidthWithBottomMargin(int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(bottomMarginDp));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
