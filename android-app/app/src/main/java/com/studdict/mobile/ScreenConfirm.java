package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.ReservationRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenConfirm extends Activity {

    private long venueId;
    private long tableId;
    private String venueName, date, time, subject;
    private int duration, capacity, tableNumber;
    private boolean isPublic;
    private String studentId = "S1"; // Hardcoded for mockup
    private long reservationIdToModify = -1L;
    private boolean isModifyMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_confirm);

        Intent i = getIntent();
        reservationIdToModify = i.getLongExtra("RESERVATION_ID", -1L);
        
        if (reservationIdToModify != -1L) {
            isModifyMode = true;
            date = i.getStringExtra("NEW_DATE");
            time = i.getStringExtra("NEW_TIME");
            duration = i.getIntExtra("NEW_DURATION", 120);
            venueName = "Current Venue"; 
            tableNumber = 5; // Placeholder since we skipped ScreenTables
            isPublic = false;
        } else {
            venueId = i.getLongExtra("VENUE_ID", 1L);
            tableId = i.getIntExtra("TABLE_ID", -1);
            tableNumber = i.getIntExtra("TABLE_NUMBER", 1);
            venueName = i.getStringExtra("VENUE_NAME");
            date = i.getStringExtra("DATE");
            time = i.getStringExtra("TIME");
            duration = i.getIntExtra("DURATION", 120);
            capacity = i.getIntExtra("CAPACITY", 1);
            isPublic = i.getBooleanExtra("IS_PUBLIC", false);
            subject = i.getStringExtra("SUBJECT");
        }

        TextView summaryVenue = findViewById(R.id.summaryVenue);
        TextView summaryTable = findViewById(R.id.summaryTable);
        TextView summaryDate = findViewById(R.id.summaryDate);
        TextView summaryTime = findViewById(R.id.summaryTime);
        TextView summaryDuration = findViewById(R.id.summaryDuration);
        TextView summaryMode = findViewById(R.id.summaryMode);

        summaryVenue.setText(venueName);
        summaryTable.setText("Table " + tableNumber);
        summaryDate.setText("Date: " + date);
        summaryTime.setText("Time: " + time);
        summaryDuration.setText("Duration: " + (duration / 60) + " Hours");
        summaryMode.setText("Mode: " + (isPublic ? "Public (" + subject + ")" : "Private"));

        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        Button confirmBtn = findViewById(R.id.confirmButton);
        confirmBtn.setOnClickListener(view -> submitReservation());

        android.view.View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, ScreenVenues.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }

    private void submitReservation() {
        if (isModifyMode) {
            com.studdict.mobile.model.ReservationUpdateRequest req = new com.studdict.mobile.model.ReservationUpdateRequest(time, duration);
            ApiClient.getApi().modifyReservation(reservationIdToModify, req).enqueue(new Callback<com.studdict.mobile.model.Reservation>() {
                @Override
                public void onResponse(Call<com.studdict.mobile.model.Reservation> call, Response<com.studdict.mobile.model.Reservation> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ScreenConfirm.this, "Successfully modified reservation!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ScreenConfirm.this, ScreenVenues.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ScreenConfirm.this, "Failed to modify. HTTP " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<com.studdict.mobile.model.Reservation> call, Throwable t) {
                    Toast.makeText(ScreenConfirm.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        ReservationRequest request = new ReservationRequest(
                studentId,
                (int) tableId,
                date,
                time,
                duration,
                capacity,
                subject
        );

        Call<Long> call;
        if (isPublic) {
            call = ApiClient.getApi().createPublicReservation(request);
        } else {
            call = ApiClient.getApi().createPrivateReservation(request);
        }

        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long reservationId = response.body();

                    Toast.makeText(
                            ScreenConfirm.this,
                            "Reservation confirmed #" + reservationId,
                            Toast.LENGTH_LONG
                    ).show();

                    Intent intent = new Intent(ScreenConfirm.this, ScreenReservationDetails.class);
                    intent.putExtra("RESERVATION_ID", reservationId);
                    intent.putExtra("TABLE_ID", tableId);
                    intent.putExtra("TABLE_NUMBER", tableNumber);
                    intent.putExtra("VENUE_NAME", venueName);
                    intent.putExtra("DATE", date);
                    intent.putExtra("TIME", time);
                    intent.putExtra("DURATION", duration);
                    intent.putExtra("CAPACITY", capacity);
                    intent.putExtra("STUDENT_ID", studentId);

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ScreenConfirm.this, "Failed. HTTP " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(ScreenConfirm.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}