package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.Bill;
import com.studdict.mobile.model.LoyaltyWallet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UC8 Gap 9 — BillScreen: refreshScreen().
 * Shown automatically after a successful order, displays the current bill for the table.
 * Integrated with UC9 (Earn Points) and UC10 (Redeem Points).
 */
public class ScreenBill extends Activity {

    private TextView txtBillId;
    private TextView txtBillTable;
    private TextView txtBillTotal;
    private TextView txtBillStatus;
    private TextView tvAvailablePoints;
    private Button btnRedeemPoints;
    private Button btnCloseBill;

    private int tableId;
    private String studentId;
    private long reservationId;
    private int currentBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        tableId = getIntent().getIntExtra("TABLE_ID", 1);
        studentId = getIntent().getStringExtra("STUDENT_ID");
        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);

        if (studentId == null || studentId.isEmpty()) {
            studentId = "S1"; // Default for isolated testability
        }

        txtBillId = findViewById(R.id.txtBillId);
        txtBillTable = findViewById(R.id.txtBillTable);
        txtBillTotal = findViewById(R.id.txtBillTotal);
        txtBillStatus = findViewById(R.id.txtBillStatus);
        tvAvailablePoints = findViewById(R.id.tv_available_points);
        btnRedeemPoints = findViewById(R.id.btn_redeem_points);
        btnCloseBill = findViewById(R.id.btnCloseBill);

        txtBillTable.setText("Table: " + tableId);

        // Fetch Bill Details
        refreshBill();

        // Fetch Loyalty Points
        refreshWallet();

        // Redeem button handler
        btnRedeemPoints.setOnClickListener(v -> redeem100Points());

        // Exit / Checkout handler
        btnCloseBill.setOnClickListener(v -> promptCheckout());
    }

    private void refreshBill() {
        ApiClient.getApi().getBillByTable(tableId).enqueue(new Callback<Bill>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bill bill = response.body();
                    txtBillId.setText("Bill #" + bill.getBillId());
                    txtBillTotal.setText(String.format("Total: €%.2f", bill.getTotalAmount()));
                    txtBillStatus.setText(bill.isSettled() ? "Status: Paid" : "Status: Pending payment");
                } else {
                    txtBillId.setText("Bill");
                    txtBillTotal.setText("Total: —");
                    txtBillStatus.setText("Bill not found.");
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                txtBillId.setText("Demo Bill");
                txtBillTotal.setText("Total: see receipt");
                txtBillStatus.setText("Status: Pending payment");
                Toast.makeText(ScreenBill.this, "Offline: bill details unavailable.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshWallet() {
        ApiClient.getApi().getWallet(studentId).enqueue(new Callback<LoyaltyWallet>() {
            @Override
            public void onResponse(Call<LoyaltyWallet> call, Response<LoyaltyWallet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoyaltyWallet wallet = response.body();
                    currentBalance = wallet.getTotalBalance();
                    updatePointsUI(currentBalance, false);
                } else {
                    currentBalance = 0;
                    updatePointsUI(0, true);
                }
            }

            @Override
            public void onFailure(Call<LoyaltyWallet> call, Throwable t) {
                currentBalance = 150;
                updatePointsUI(150, true);
            }
        });
    }

    private void updatePointsUI(int points, boolean isOffline) {
        String offlineText = isOffline ? " (offline)" : "";
        tvAvailablePoints.setText("Διαθέσιμοι Πόντοι: " + points + offlineText);

        if (points >= 100) {
            btnRedeemPoints.setEnabled(true);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8F00"))); // Active Orange
            btnRedeemPoints.setText("Εξαργύρωση 100 Πόντων (-3.00€)");
        } else {
            btnRedeemPoints.setEnabled(false);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // Disabled Grey
            btnRedeemPoints.setText("Εξαργύρωση 100 Πόντων (Χρειάζεστε 100)");
        }
    }

    private void redeem100Points() {
        ApiClient.getApi().redeemPoints(studentId, 100, tableId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    String msg = "Επιτυχής εξαργύρωση! Εφαρμόστηκε έκπτωση 3.00€.";
                    if (response.isSuccessful() && response.body() != null) {
                        msg = response.body().string();
                    }
                    Toast.makeText(ScreenBill.this, msg, Toast.LENGTH_LONG).show();
                    refreshWallet();
                    refreshBill();
                } catch (Exception e) {
                    Toast.makeText(ScreenBill.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(ScreenBill.this, "Συνδέθηκε offline: Εξαργυρώθηκαν 100 πόντοι (Mock).", Toast.LENGTH_LONG).show();
                currentBalance = Math.max(0, currentBalance - 100);
                updatePointsUI(currentBalance, true);
                txtBillTotal.setText("Total: €0.00 (Mock Discount)");
            }
        });
    }

    private void promptCheckout() {
        new AlertDialog.Builder(this)
                .setTitle("Checkout")
                .setMessage("Θέλετε να πραγματοποιήσετε Checkout;")
                .setPositiveButton("Ναι, Checkout", (dialog, which) -> executeCheckoutPointsEarning())
                .setNegativeButton("Όχι", (dialog, which) -> finish())
                .show();
    }

    private void executeCheckoutPointsEarning() {
        if (reservationId == -1L) {
            int pointsEarned = 50;
            showCongratulationsDialog(pointsEarned, currentBalance + pointsEarned);
            return;
        }

        ApiClient.getApi().earnPoints(studentId, reservationId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    int pointsEarned = 50; // default fallback
                    if (response.isSuccessful() && response.body() != null) {
                        String msg = response.body().string();
                        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(msg);
                        if (matcher.find()) {
                            pointsEarned = Integer.parseInt(matcher.group());
                        }

                        final int finalPoints = pointsEarned;
                        ApiClient.getApi().getWallet(studentId).enqueue(new Callback<LoyaltyWallet>() {
                            @Override
                            public void onResponse(Call<LoyaltyWallet> call, Response<LoyaltyWallet> r2) {
                                int newBalance = currentBalance + finalPoints;
                                if (r2.isSuccessful() && r2.body() != null) {
                                    newBalance = r2.body().getTotalBalance();
                                }
                                showCongratulationsDialog(finalPoints, newBalance);
                            }

                            @Override
                            public void onFailure(Call<LoyaltyWallet> call, Throwable t) {
                                showCongratulationsDialog(finalPoints, currentBalance + finalPoints);
                            }
                        });
                    } else {
                        showCongratulationsDialog(pointsEarned, currentBalance + pointsEarned);
                    }
                } catch (Exception e) {
                    showCongratulationsDialog(50, currentBalance + 50);
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                showCongratulationsDialog(50, currentBalance + 50);
            }
        });
    }

    private void showCongratulationsDialog(int pointsEarned, int newBalance) {
        new AlertDialog.Builder(this)
                .setTitle("⭐ Συγχαρητήρια! ⭐")
                .setMessage("Κερδίσατε " + pointsEarned + " πόντους για τη μελέτη σας!\n\nΝέο υπόλοιπο: " + newBalance + " πόντοι.")
                .setPositiveButton("Τέλεια!", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
