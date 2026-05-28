package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.Reservation;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenMyBookings extends Activity {

    private LinearLayout myBookingsContainer;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        studentId = getIntent().getStringExtra("STUDENT_ID");

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        myBookingsContainer = findViewById(R.id.myBookingsContainer);

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "No Student ID provided", Toast.LENGTH_SHORT).show();
            // Defaulting to "S1" for testing if absent
            studentId = "S1";
        }

        fetchMyBookings();
    }

    private void fetchMyBookings() {
        ApiClient.getApi().getStudentReservations(studentId).enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateBookings(response.body());
                } else {
                    Toast.makeText(ScreenMyBookings.this, "Failed to fetch bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Toast.makeText(ScreenMyBookings.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateBookings(List<Reservation> reservations) {
        myBookingsContainer.removeAllViews();

        if (reservations.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("You have no active bookings.");
            empty.setTextSize(18);
            empty.setTextColor(Color.GRAY);
            myBookingsContainer.addView(empty);
            return;
        }

        for (Reservation res : reservations) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(32, 32, 32, 32);
            card.setBackgroundResource(R.drawable.choice_unselected); // using existing drawable for card background

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);

            TextView info = new TextView(this);
            String tableInfo = res.getTable() != null ? "Table " + res.getTable().getTableNumber() : "Unknown Table";
            info.setText("Date: " + res.getReservationDate() + "\n" +
                    "Time: " + res.getStartTime() + "\n" +
                    "Duration: " + (res.getDurationMinutes() / 60) + "h\n" +
                    tableInfo + "\n" +
                    "Status: " + res.getStatus());
            info.setTextSize(16);
            info.setTextColor(Color.BLACK);
            info.setPadding(0, 0, 0, 16);
            card.addView(info);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button btnModify = new Button(this);
            btnModify.setText("MODIFY");
            btnModify.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btnModify.setOnClickListener(v -> {
                Intent intent = new Intent(ScreenMyBookings.this, ScreenForm.class);
                intent.putExtra("RESERVATION_ID", res.getReservationId());
                intent.putExtra("VENUE_ID", 1L); // Hardcoding to 1L because Venue is not mapped in Android StudyTable
                intent.putExtra("STUDENT_ID", studentId);
                startActivity(intent);
            });
            buttonLayout.addView(btnModify);

            Button btnCheckIn = new Button(this);
            btnCheckIn.setText("CHECK-IN");
            btnCheckIn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btnCheckIn.setOnClickListener(v -> {
                Intent intent = new Intent(ScreenMyBookings.this, ScreenCheckIn.class);
                intent.putExtra("RESERVATION_ID", res.getReservationId());
                intent.putExtra("STUDENT_ID", studentId);

                if (res.getTable() != null) {
                    intent.putExtra("TABLE_NUMBER", res.getTable().getTableNumber());
                    intent.putExtra("DUMMY_QR", res.getTable().getQrCodeString());
                }

                startActivity(intent);
            });
            buttonLayout.addView(btnCheckIn);

            Button btnCheckout = new Button(this);
            btnCheckout.setText("CHECKOUT");
            btnCheckout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            btnCheckout.setOnClickListener(v -> {
                Toast.makeText(ScreenMyBookings.this, "Proceeding to Checkout...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ScreenMyBookings.this, ScreenBill.class);
                intent.putExtra("TABLE_ID", res.getTable() != null ? (int) res.getTable().getId() : 1);
                intent.putExtra("STUDENT_ID", studentId);
                intent.putExtra("RESERVATION_ID", res.getReservationId());
                startActivity(intent);
            });
            buttonLayout.addView(btnCheckout);

            card.addView(buttonLayout);
            myBookingsContainer.addView(card);
        }
    }
}
