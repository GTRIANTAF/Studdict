package com.studdict.mobile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.PublicReservation;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenLiveBoard extends Activity {

    private LinearLayout liveBoardContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_board);

        liveBoardContainer = findViewById(R.id.liveBoardContainer);

        setupBottomNavigation();
        fetchPublishedReservations();
    }

    private void fetchPublishedReservations() {
        ApiClient.getApi().getPublishedReservations().enqueue(new Callback<List<PublicReservation>>() {
            @Override
            public void onResponse(Call<List<PublicReservation>> call, Response<List<PublicReservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateLiveBoard(response.body());
                } else {
                    Toast.makeText(ScreenLiveBoard.this, "Failed to load Live Board", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PublicReservation>> call, Throwable t) {
                Toast.makeText(ScreenLiveBoard.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateLiveBoard(List<PublicReservation> reservations) {
        liveBoardContainer.removeAllViews();

        if (reservations.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No active public reservations right now.");
            emptyText.setTextSize(16);
            liveBoardContainer.addView(emptyText);
            return;
        }

        for (PublicReservation res : reservations) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(32, 32, 32, 32);
            row.setBackgroundColor(Color.WHITE);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            row.setLayoutParams(params);

            TextView infoText = new TextView(this);
            String subjectName = res.getStudySubject() != null ? res.getStudySubject().getName() : "General Study";
            String tableInfo = res.getTable() != null ? "Table " + res.getTable().getTableNumber() + " (Capacity: " + res.getTable().getCapacity() + ")" : "Unknown Table";
            
            infoText.setText(subjectName + "\n" + tableInfo);
            infoText.setTextSize(16);
            infoText.setTextColor(Color.BLACK);
            row.addView(infoText);

            liveBoardContainer.addView(row);
        }
    }

    private void setupBottomNavigation() {
        android.view.View navHome = findViewById(R.id.navHome);
        if (navHome != null) navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenVenues.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        android.view.View navLiveBoard = findViewById(R.id.navLiveBoard);
        if (navLiveBoard != null) navLiveBoard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenLiveBoard.class);
            startActivity(intent);
        });

        android.view.View navMyBookings = findViewById(R.id.navMyBookings);
        if (navMyBookings != null) navMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenMyBookings.class);
            startActivity(intent);
        });

        android.view.View navOrder = findViewById(R.id.navOrder);
        if (navOrder != null) navOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenOrderMenu.class);
            startActivity(intent);
        });

        android.view.View navEbook = findViewById(R.id.navEbook);
        if (navEbook != null) navEbook.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenEBookVault.class);
            startActivity(intent);
        });
    }

}
