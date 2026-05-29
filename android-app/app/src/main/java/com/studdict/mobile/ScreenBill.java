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
    private TextView txtEbookLoansHeader;
    private LinearLayout ebookLoansContainer;
    private Button btnRedeemPoints;
    private Button btnCloseBill;
    private android.widget.Button btnSplitBill;
    private android.widget.TextView tvSplitResult;
    private android.widget.Button btnPayOnline;
    private android.widget.Button btnPayCash;

    private int tableId;
    private String studentId;
    private long reservationId;
    private int currentBalance = 0;
    private Long currentBillId = -1L;
    private double currentBillTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        tableId = getIntent().getIntExtra("TABLE_ID", 1);

        txtBillId = findViewById(R.id.txtBillId);
        txtBillTable = findViewById(R.id.txtBillTable);
        txtBillTotal = findViewById(R.id.txtBillTotal);
        txtBillStatus = findViewById(R.id.txtBillStatus);
        txtEbookLoansHeader = findViewById(R.id.txtEbookLoansHeader);
        ebookLoansContainer = findViewById(R.id.ebookLoansContainer);
        tvAvailablePoints = findViewById(R.id.tv_available_points);
        btnRedeemPoints = findViewById(R.id.btn_redeem_points);
        btnCloseBill = findViewById(R.id.btnCloseBill);
        
        btnSplitBill = findViewById(R.id.btn_split_bill);
        tvSplitResult = findViewById(R.id.tv_split_result);
        btnPayOnline = findViewById(R.id.btn_pay_online);
        btnPayCash = findViewById(R.id.btn_pay_cash);

        txtBillTable.setText("Table: " + tableId);

        studentId = getIntent().getStringExtra("STUDENT_ID");
        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);

        if (studentId == null || studentId.isEmpty()) {
            studentId = "S1"; // Default for isolated testability
        }

        // Fetch Bill Details
        refreshBill();

        // Fetch Loyalty Points
        refreshWallet();

        // Show e-books borrowed this session
        loadEbookLoans();

        // Redeem button handler
        btnRedeemPoints.setOnClickListener(v -> redeem100Points());
        
        btnSplitBill.setOnClickListener(v -> handleSplitBill());
        btnPayOnline.setOnClickListener(v -> handlePayOnline());
        btnPayCash.setOnClickListener(v -> handlePayCash());
        
        // Exit without payment
        btnCloseBill.setOnClickListener(v -> finish());
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
                    currentBillId = bill.getBillId();
                    currentBillTotal = bill.getTotalAmount();
                    txtBillId.setText("Bill #" + bill.getBillId());
                    txtBillTotal.setText(String.format("Total: €%.2f", bill.getTotalAmount()));
                    txtBillStatus.setText(bill.isSettled() ? "Status: Paid" : "Status: Pending payment");
                } else {
                    currentBillId = -1L;
                    currentBillTotal = 0.0;
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

    private void handleSplitBill() {
        if (currentBillId == -1L) {
            Toast.makeText(this, "No active bill found.", Toast.LENGTH_SHORT).show();
            return;
        }

        com.studdict.mobile.model.SplitRequest req = new com.studdict.mobile.model.SplitRequest(currentBillId);
        ApiClient.getApi().splitBill(req).enqueue(new Callback<com.studdict.mobile.model.SplitResponse>() {
            @Override
            public void onResponse(Call<com.studdict.mobile.model.SplitResponse> call, Response<com.studdict.mobile.model.SplitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double perPerson = response.body().getAmountPerPerson();
                    
                    // UPDATE current bill total so payment uses this
                    currentBillTotal = perPerson;

                    tvSplitResult.setVisibility(View.VISIBLE);
                    tvSplitResult.setText(String.format("Split Amount: €%.2f per person", perPerson));
                    txtBillTotal.setText(String.format("Total: €%.2f (Split)", perPerson));
                    
                } else {
                    Toast.makeText(ScreenBill.this, "Failed to split bill", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.studdict.mobile.model.SplitResponse> call, Throwable t) {
                Toast.makeText(ScreenBill.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePayOnline() {
        if (currentBillId == -1L) {
            Toast.makeText(this, "No active bill found or offline mode.", Toast.LENGTH_SHORT).show();
            executeCheckoutPointsEarning(); // fallback for offline testing
            return;
        }
        processPayment("CARD", currentBillTotal);
    }

    private void handlePayCash() {
        if (currentBillId == -1L) {
            Toast.makeText(this, "No active bill found or offline mode.", Toast.LENGTH_SHORT).show();
            executeCheckoutPointsEarning(); // fallback for offline testing
            return;
        }
        promptCashAmount();
    }

    private void promptCashAmount() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount given");
        input.setText(String.valueOf(currentBillTotal)); // Pre-fill with the exact amount required

        new AlertDialog.Builder(this)
                .setTitle("Cash Payment")
                .setMessage("Total: €" + currentBillTotal)
                .setView(input)
                .setPositiveButton("Pay", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(val);
                            if (amount > 0) {
                                processPayment("CASH", amount);
                            } else {
                                Toast.makeText(this, "Amount must be > 0!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment(String method, double amountGiven) {
        com.studdict.mobile.model.PaymentRequest req = new com.studdict.mobile.model.PaymentRequest(currentBillId, method, amountGiven, studentId);
        ApiClient.getApi().processPayment(req).enqueue(new Callback<com.studdict.mobile.model.PaymentResponse>() {
            @Override
            public void onResponse(Call<com.studdict.mobile.model.PaymentResponse> call, Response<com.studdict.mobile.model.PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ScreenBill.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    checkEarnedPointsAfterPayment();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Payment failed";
                    Toast.makeText(ScreenBill.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.studdict.mobile.model.PaymentResponse> call, Throwable t) {
                Toast.makeText(ScreenBill.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEarnedPointsAfterPayment() {
        ApiClient.getApi().getWallet(studentId).enqueue(new Callback<LoyaltyWallet>() {
            @Override
            public void onResponse(Call<LoyaltyWallet> call, Response<LoyaltyWallet> response) {
                int newBalance = currentBalance;
                int earned = 0;
                if (response.isSuccessful() && response.body() != null) {
                    newBalance = response.body().getTotalBalance();
                    earned = newBalance - currentBalance;
                }
                if (earned <= 0 && reservationId != -1L) earned = 50; 
                showCongratulationsDialog(earned, newBalance);
            }

            @Override
            public void onFailure(Call<LoyaltyWallet> call, Throwable t) {
                showCongratulationsDialog(50, currentBalance + 50);
            }
        });
    }

    // fallback for offline or no-bill cases
    private void executeCheckoutPointsEarning() {
        showCongratulationsDialog(50, currentBalance + 50);
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
