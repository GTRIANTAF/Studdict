package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.LoyaltyWallet;
import com.studdict.mobile.model.PointsTransaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenVenues extends Activity {

    private String studentId;
    private TextView txtWalletPoints;
    private int currentPointsBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        studentId = getIntent().getStringExtra("STUDENT_ID");
        if (studentId == null || studentId.isEmpty()) {
            SessionManager session = new SessionManager(this);
            studentId = session.getStudentId();
        }
        if (studentId == null || studentId.isEmpty()) {
            studentId = "S2"; // Default seeded student ID (Maria)
        }

        String studentName = getIntent().getStringExtra("STUDENT_NAME");
        TextView greetingText = findViewById(R.id.greetingText);
        if (studentName != null && !studentName.isEmpty() && greetingText != null) {
            greetingText.setText("Hey " + studentName + "!");
        }

        txtWalletPoints = findViewById(R.id.txtWalletPoints);
        View layoutWallet = findViewById(R.id.layoutWallet);
        if (layoutWallet != null) {
            layoutWallet.setOnClickListener(v -> showPointsHistoryDialog());
        }

        LinearLayout venueCeid = findViewById(R.id.venueCeid);
        LinearLayout venueCafe = findViewById(R.id.venueCafe);

        venueCeid.setOnClickListener(v -> selectVenue(1L, "CEID LIBRARY"));
        venueCafe.setOnClickListener(v -> selectVenue(2L, "PATRAS CITY CAFE"));

        View navLiveBoard = findViewById(R.id.navLiveBoard);
        navLiveBoard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenLiveBoard.class);
            startActivity(intent);
        });

        View navMyBookings = findViewById(R.id.navMyBookings);
        navMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenMyBookings.class);
            intent.putExtra("STUDENT_ID", studentId);
            startActivity(intent);
        });

        View navOrder = findViewById(R.id.navOrder);
        navOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenOrderMenu.class);
            startActivity(intent);
        });

        View navEbook = findViewById(R.id.navEbook);
        navEbook.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenEBookVault.class);
            startActivity(intent);
        });

        View btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenProfile.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchWalletBalance();
    }

    private void fetchWalletBalance() {
        if (studentId == null || studentId.isEmpty()) return;
        ApiClient.getApi().getWallet(studentId).enqueue(new Callback<LoyaltyWallet>() {
            @Override
            public void onResponse(Call<LoyaltyWallet> call, Response<LoyaltyWallet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPointsBalance = response.body().getTotalBalance();
                    if (txtWalletPoints != null) {
                        txtWalletPoints.setText(currentPointsBalance + " pts");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoyaltyWallet> call, Throwable t) {
                if (txtWalletPoints != null) {
                    txtWalletPoints.setText("--- pts");
                }
            }
        });
    }

    private void showPointsHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_points_history, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView dialogWalletBalance = dialogView.findViewById(R.id.dialogWalletBalance);
        dialogWalletBalance.setText("Current Balance: " + currentPointsBalance + " pts");

        LinearLayout historyContainer = dialogView.findViewById(R.id.historyContainer);
        ProgressBar historyLoader = dialogView.findViewById(R.id.historyLoader);
        TextView txtNoHistory = dialogView.findViewById(R.id.txtNoHistory);
        Button btnCloseHistory = dialogView.findViewById(R.id.btnCloseHistory);

        btnCloseHistory.setOnClickListener(v -> dialog.dismiss());

        // Fetch Points Transaction History
        ApiClient.getApi().getPointsHistory(studentId).enqueue(new Callback<List<PointsTransaction>>() {
            @Override
            public void onResponse(Call<List<PointsTransaction>> call, Response<List<PointsTransaction>> response) {
                if (historyLoader != null) historyLoader.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<PointsTransaction> transactions = response.body();
                    if (txtNoHistory != null) txtNoHistory.setVisibility(View.GONE);
                    if (historyContainer != null) {
                        historyContainer.removeAllViews();
                        for (PointsTransaction tx : transactions) {
                            View row = inflater.inflate(R.layout.item_points_transaction, null);
                            
                            TextView txIcon = row.findViewById(R.id.txIcon);
                            TextView txTitle = row.findViewById(R.id.txTitle);
                            TextView txDescription = row.findViewById(R.id.txDescription);
                            TextView txTimestamp = row.findViewById(R.id.txTimestamp);
                            TextView txAmount = row.findViewById(R.id.txAmount);

                            boolean isEarn = "EARN".equalsIgnoreCase(tx.getTransactionType());
                            txIcon.setText(isEarn ? "🟢" : "🔴");
                            txTitle.setText(isEarn ? "Study Session Gained" : "Discount Redeemed");
                            txDescription.setText(tx.getDescription() != null ? tx.getDescription() : "");
                            
                            // Format timestamp nicely
                            String rawTime = tx.getTimestamp();
                            if (rawTime != null && rawTime.contains("T")) {
                                rawTime = rawTime.replace("T", " ").substring(0, Math.min(rawTime.length(), 16));
                            }
                            txTimestamp.setText(rawTime != null ? rawTime : "");
                            
                            txAmount.setText((isEarn ? "+" : "-") + tx.getPointsAmount() + " pts");
                            txAmount.setTextColor(Color.parseColor(isEarn ? "#10B981" : "#EF4444"));

                            historyContainer.addView(row);
                        }
                    }
                } else {
                    if (txtNoHistory != null) txtNoHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<PointsTransaction>> call, Throwable t) {
                if (historyLoader != null) historyLoader.setVisibility(View.GONE);
                if (txtNoHistory != null) {
                    txtNoHistory.setText("Failed to load history.");
                    txtNoHistory.setVisibility(View.VISIBLE);
                }
                Toast.makeText(ScreenVenues.this, "Offline: points log unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void selectVenue(long venueId, String venueName) {
        Intent intent = new Intent(this, ScreenForm.class);
        intent.putExtra("VENUE_ID", venueId);
        intent.putExtra("VENUE_NAME", venueName);
        intent.putExtra("STUDENT_ID", studentId);
        startActivity(intent);
    }
}
