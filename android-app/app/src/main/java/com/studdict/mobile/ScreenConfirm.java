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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_confirm);

        Intent i = getIntent();
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
    }

    private void submitReservation() {
        ReservationRequest request = new ReservationRequest(
                studentId,
                (int)tableId,
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
                    Long resId = response.body();
                    Toast.makeText(ScreenConfirm.this, "Success! Reservation #" + resId, Toast.LENGTH_LONG).show();
                    
                    // Return to home screen
                    Intent intent = new Intent(ScreenConfirm.this, ScreenVenues.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
