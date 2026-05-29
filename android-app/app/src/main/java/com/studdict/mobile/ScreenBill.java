package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.Bill;
import com.studdict.mobile.model.EBookLoanInfo;
import com.studdict.mobile.model.LoyaltyWallet;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UC8 — BillScreen: refreshScreen().
 * Displays the current bill for the table. The bill is updated/refreshed on the backend
 * as soon as an order is placed, but this screen is shown only when the student chooses
 * to checkout (e.g. from "My Bookings") — it is not opened automatically after ordering.
 * Integrated with UC9 (Earn Points) and UC10 (Redeem Points).
 */
public class ScreenBill extends Activity {

    private TextView txtBillId;
    private TextView txtBillTable;
    private TextView txtBillTotal;
    private TextView txtBillStatus;
    private TextView tvAvailablePoints;
    private TextView txtEbookLoansHeader;
    private LinearLayout ebookLoansContainer;
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
        txtEbookLoansHeader = findViewById(R.id.txtEbookLoansHeader);
        ebookLoansContainer = findViewById(R.id.ebookLoansContainer);
        tvAvailablePoints = findViewById(R.id.tv_available_points);
        btnRedeemPoints = findViewById(R.id.btn_redeem_points);
        btnCloseBill = findViewById(R.id.btnCloseBill);

        txtBillTable.setText("Table: " + tableId);

        // Fetch Bill Details
        refreshBill();

        // Fetch Loyalty Points
        refreshWallet();

        // Show e-books borrowed this session
        loadEbookLoans();

        // Redeem button handler
        btnRedeemPoints.setOnClickListener(v -> redeem100Points());

        // Exit / Checkout handler
        btnCloseBill.setOnClickListener(v -> promptCheckout());
    }

    private void loadEbookLoans() {
        long checkInId = new SessionManager(this).getCheckInId();
        if (checkInId == -1L) return;

        ApiClient.getApi().getSessionLoans(checkInId).enqueue(new Callback<List<EBookLoanInfo>>() {
            @Override
            public void onResponse(Call<List<EBookLoanInfo>> call, Response<List<EBookLoanInfo>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    showEbookLoans(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<EBookLoanInfo>> call, Throwable t) {
                // Offline — no loan data to display
            }
        });
    }

    private void showEbookLoans(List<EBookLoanInfo> loans) {
        txtEbookLoansHeader.setVisibility(View.VISIBLE);
        ebookLoansContainer.removeAllViews();
        for (EBookLoanInfo loan : loans) {
            TextView tv = new TextView(this);
            String label = "• " + loan.getTitle() + " — " + loan.getAuthor();
            if (loan.isReturned()) {
                label += "  (returned)";
            }
            tv.setText(label);
            tv.setTextSize(14);
            tv.setTextColor(Color.parseColor(loan.isReturned() ? "#9E9E9E" : "#4A148C"));
            tv.setPadding(0, 4, 0, 4);
            ebookLoansContainer.addView(tv);
        }
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
        tvAvailablePoints.setText("Available Points: " + points + offlineText);

        if (points >= 100) {
            btnRedeemPoints.setEnabled(true);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8F00"))); // Active Orange
            btnRedeemPoints.setText("Redeem 100 Points (-3.00€)");
        } else {
            btnRedeemPoints.setEnabled(false);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // Disabled Grey
            btnRedeemPoints.setText("Redeem 100 Points (Need 100)");
        }
    }

    private void redeem100Points() {
        ApiClient.getApi().redeemPoints(studentId, 100, tableId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    String msg = "Redemption successful! A 3.00€ discount was applied.";
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
                Toast.makeText(ScreenBill.this, "Connected offline: Redeemed 100 points (Mock).", Toast.LENGTH_LONG).show();
                currentBalance = Math.max(0, currentBalance - 100);
                updatePointsUI(currentBalance, true);
                txtBillTotal.setText("Total: €0.00 (Mock Discount)");
            }
        });
    }

    private void promptCheckout() {
        new AlertDialog.Builder(this)
                .setTitle("Checkout")
                .setMessage("Do you want to checkout?")
                .setPositiveButton("Yes, Checkout", (dialog, which) -> executeCheckoutPointsEarning())
                .setNegativeButton("No", (dialog, which) -> finish())
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
                .setTitle("⭐ Congratulations! ⭐")
                .setMessage("You earned " + pointsEarned + " points for your study session!\n\nNew balance: " + newBalance + " points.")
                .setPositiveButton("Awesome!", (dialog, which) -> completeCheckout())
                .setCancelable(false)
                .show();
    }

    private void completeCheckout() {
        SessionManager session = new SessionManager(this);
        long checkInId = session.getCheckInId();
        if (checkInId != -1L) {
            // UC7 Standard Checkout: release any active ebook loans (fire and forget)
            ApiClient.getApi().notifyCheckout(checkInId).enqueue(new Callback<String>() {
                @Override public void onResponse(Call<String> call, Response<String> response) {}
                @Override public void onFailure(Call<String> call, Throwable t) {}
            });
        }
        session.clearCheckIn();
        finish();
    }
}
