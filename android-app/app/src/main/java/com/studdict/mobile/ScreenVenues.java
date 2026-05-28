package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenVenues extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        String studentName = getIntent().getStringExtra("STUDENT_NAME");
        TextView greetingText = findViewById(R.id.greetingText);
        if (studentName != null && !studentName.isEmpty() && greetingText != null) {
            greetingText.setText("Hello, " + studentName + "! 👋");
        }

        LinearLayout venueCeid = findViewById(R.id.venueCeid);
        LinearLayout venueCafe = findViewById(R.id.venueCafe);

        venueCeid.setOnClickListener(v -> selectVenue(1L, "CEID LIBRARY"));
        venueCafe.setOnClickListener(v -> selectVenue(2L, "PATRAS CITY CAFE"));

        Button modifyBtn = findViewById(R.id.modifyReservationBtn);
        if (modifyBtn != null) {
            modifyBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, ScreenForm.class);
                intent.putExtra("IS_MODIFY", true);
                intent.putExtra("RESERVATION_ID", 100L); // Mock reservation ID
                startActivity(intent);
            });
        }

        View navLiveBoard = findViewById(R.id.navLiveBoard);
        navLiveBoard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenLiveBoard.class);
            startActivity(intent);
        });

        View navOrder = findViewById(R.id.navOrder);
        navOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenOrderMenu.class);
            startActivity(intent);
        });

        View navEbook = findViewById(R.id.navEbook);
        navEbook.setOnClickListener(v -> {
            // UC7: Start - Select Digital Vault & Check-in Verification
            long mockCheckInId = 1L; // Mock check-in ID
            ApiClient.getApi().requestAccess(mockCheckInId).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                        Intent intent = new Intent(ScreenVenues.this, ScreenEBookVault.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ScreenVenues.this, "Check-in Required", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(ScreenVenues.this, "Network Error: Check-in verification failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        View navCheckout = findViewById(R.id.navCheckout);
        if (navCheckout != null) {
            navCheckout.setOnClickListener(v -> {
                Intent intent = new Intent(this, ScreenBill.class);
                intent.putExtra("TABLE_ID", 1); // Pass a default or current table ID
                startActivity(intent);
            });
        }
    }

    private void selectVenue(long venueId, String venueName) {
        Intent intent = new Intent(this, ScreenForm.class);
        intent.putExtra("VENUE_ID", venueId);
        intent.putExtra("VENUE_NAME", venueName);
        startActivity(intent);
    }
}
