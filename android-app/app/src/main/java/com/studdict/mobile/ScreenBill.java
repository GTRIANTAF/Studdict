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
import android.widget.EditText;
import android.text.InputType;
import android.content.Intent;

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
    private Button btnPayFull;
    private Button btnSplitBill;
    private Button btnPayCash;
    private Button btnCloseBill;

    // Premium points stepper views
    private android.widget.ImageButton btnPointsDecrement;
    private android.widget.ImageButton btnPointsIncrement;
    private TextView tvSelectedPoints;
    private TextView tvPointsDiscount;

    // Price breakdown views
    private TextView txtOriginalPrice;
    private View layoutDiscountLine;
    private TextView txtDiscountAppliedText;

    private int tableId;
    private String studentId;
    private long reservationId;
    private int currentBalance = 0;
    private int selectedPointsToRedeem = 25;
    private Long currentBillId = null;
    private double currentTotalAmount = 0.0;
    private double grossTotalAmount = 0.0;

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
        btnPayFull = findViewById(R.id.btnPayFull);
        btnSplitBill = findViewById(R.id.btnSplitBill);
        btnPayCash = findViewById(R.id.btnPayCash);
        btnCloseBill = findViewById(R.id.btnCloseBill);

        // Bind premium points stepper views
        btnPointsDecrement = findViewById(R.id.btn_points_decrement);
        btnPointsIncrement = findViewById(R.id.btn_points_increment);
        tvSelectedPoints = findViewById(R.id.tv_selected_points);
        tvPointsDiscount = findViewById(R.id.tv_points_discount);

        // Bind price breakdown views
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        layoutDiscountLine = findViewById(R.id.layout_discount_line);
        txtDiscountAppliedText = findViewById(R.id.txtDiscountAppliedText);

        txtBillTable.setText("Table: " + tableId);

        // Fetch Bill Details
        refreshBill();

        // Fetch Loyalty Points
        refreshWallet();

        // Show e-books borrowed this session
        loadEbookLoans();

        // Stepper button click listeners
        btnPointsDecrement.setOnClickListener(v -> {
            if (selectedPointsToRedeem > 25) {
                selectedPointsToRedeem -= 25;
                updatePointsUI(currentBalance, false);
            }
        });

        btnPointsIncrement.setOnClickListener(v -> {
            boolean hasEnoughPoints = (selectedPointsToRedeem + 25) <= currentBalance;
            boolean isUnderCost = (selectedPointsToRedeem * 0.03) < grossTotalAmount;
            if (hasEnoughPoints && isUnderCost) {
                selectedPointsToRedeem += 25;
                updatePointsUI(currentBalance, false);
            }
        });

        // Redeem button handler
        btnRedeemPoints.setOnClickListener(v -> redeemSelectedPoints());

        // Checkout handlers
        btnPayFull.setOnClickListener(v -> payFullAmount());
        btnSplitBill.setOnClickListener(v -> promptSplitBill());
        btnPayCash.setOnClickListener(v -> payWithCash());
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
                    currentTotalAmount = bill.getTotalAmount();
                    txtBillId.setText("Bill #" + bill.getBillId());
                    
                    // Sleek price breakdown calculations
                    double discount = bill.getDiscountAmount();
                    double originalPrice = bill.getTotalAmount() + discount;
                    grossTotalAmount = originalPrice;
                    
                    txtOriginalPrice.setText(String.format("€%.2f", originalPrice));
                    if (discount > 0.0) {
                        layoutDiscountLine.setVisibility(View.VISIBLE);
                        txtDiscountAppliedText.setText(String.format("-€%.2f", discount));
                    } else {
                        layoutDiscountLine.setVisibility(View.GONE);
                    }
                    
                    txtBillTotal.setText(String.format("€%.2f", bill.getTotalAmount()));
                    txtBillStatus.setText(bill.isSettled() ? "Status: Paid" : "Status: Pending payment");
                    if (bill.isSettled()) {
                        btnPayFull.setEnabled(false);
                        btnSplitBill.setEnabled(false);
                        btnPayCash.setEnabled(false);
                    }
                } else {
                    txtBillId.setText("Bill");
                    txtOriginalPrice.setText("€—");
                    layoutDiscountLine.setVisibility(View.GONE);
                    txtBillTotal.setText("€—");
                    txtBillStatus.setText("Bill not found.");
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                txtBillId.setText("Demo Bill");
                txtOriginalPrice.setText("€0.00");
                layoutDiscountLine.setVisibility(View.GONE);
                txtBillTotal.setText("€0.00");
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

        // Constrain selected points to be at least 25 and at most current balance
        if (points < 25) {
            selectedPointsToRedeem = 25;
        } else if (selectedPointsToRedeem > points) {
            // Find highest multiple of 25 that is <= points
            selectedPointsToRedeem = (points / 25) * 25;
            if (selectedPointsToRedeem < 25) selectedPointsToRedeem = 25;
        }

        tvSelectedPoints.setText(selectedPointsToRedeem + " Points");
        double discountVal = selectedPointsToRedeem * 0.03;
        tvPointsDiscount.setText(String.format("-%.2f€ Discount", discountVal));

        // Enable/Disable step buttons with proper visual alpha states
        boolean canDecrement = selectedPointsToRedeem > 25;
        btnPointsDecrement.setEnabled(canDecrement);
        btnPointsDecrement.setImageAlpha(canDecrement ? 255 : 70);

        boolean canIncrement = (selectedPointsToRedeem + 25) <= points && (selectedPointsToRedeem * 0.03) < grossTotalAmount;
        btnPointsIncrement.setEnabled(canIncrement);
        btnPointsIncrement.setImageAlpha(canIncrement ? 255 : 70);

        if (points >= 25 && points >= selectedPointsToRedeem) {
            btnRedeemPoints.setEnabled(true);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF8F00"))); // Active Orange
            btnRedeemPoints.setText(String.format("Redeem %d Points (-%.2f€)", selectedPointsToRedeem, discountVal));
        } else {
            btnRedeemPoints.setEnabled(false);
            btnRedeemPoints.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BDBDBD"))); // Disabled Grey
            btnRedeemPoints.setText("Redeem Points (Need at least 25)");
        }
    }

    private void redeemSelectedPoints() {
        final int pointsToRedeem = selectedPointsToRedeem;
        ApiClient.getApi().redeemPoints(studentId, pointsToRedeem, tableId).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    String msg = "Redemption successful! A discount was applied.";
                    if (response.isSuccessful() && response.body() != null) {
                        msg = response.body().string();
                    } else if (response.errorBody() != null) {
                        msg = response.errorBody().string();
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
                Toast.makeText(ScreenBill.this, "Connected offline: Redeemed " + pointsToRedeem + " points (Mock).", Toast.LENGTH_LONG).show();
                currentBalance = Math.max(0, currentBalance - pointsToRedeem);
                updatePointsUI(currentBalance, true);
                double discount = pointsToRedeem * 0.03;
                txtBillTotal.setText(String.format("Total: €0.00 (Mock -%.2f€ Discount)", discount));
            }
        });
    }

    // --- UC6 Checkout Methods ---

    private void payFullAmount() {
        if (currentBillId == null) {
            Toast.makeText(this, "Bill not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ScreenPayment.class);
        intent.putExtra("BILL_ID", currentBillId);
        intent.putExtra("AMOUNT", currentTotalAmount);
        intent.putExtra("STUDENT_ID", studentId);
        intent.putExtra("RESERVATION_ID", reservationId);
        startActivity(intent);
        finish();
    }

    private void promptSplitBill() {
        if (currentBillId == null) {
            Toast.makeText(this, "Bill not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reservationId == -1L) {
            showManualSplitDialog();
            return;
        }

        Toast.makeText(this, "Detecting participants...", Toast.LENGTH_SHORT).show();
        ApiClient.getApi().getParticipants(reservationId).enqueue(new Callback<List<com.studdict.mobile.model.ReservationParticipant>>() {
            @Override
            public void onResponse(Call<List<com.studdict.mobile.model.ReservationParticipant>> call, Response<List<com.studdict.mobile.model.ReservationParticipant>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    showParticipantsSplitDialog(response.body());
                } else {
                    showManualSplitDialog();
                }
            }

            @Override
            public void onFailure(Call<List<com.studdict.mobile.model.ReservationParticipant>> call, Throwable t) {
                showManualSplitDialog();
            }
        });
    }

    private void showParticipantsSplitDialog(List<com.studdict.mobile.model.ReservationParticipant> participants) {
        String[] participantNames = new String[participants.size()];
        boolean[] checkedItems = new boolean[participants.size()];
        for (int i = 0; i < participants.size(); i++) {
            participantNames[i] = "Student " + participants.get(i).getStudentId() + " (" + participants.get(i).getRole() + ")";
            checkedItems[i] = false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Participants to Split With");
        builder.setMultiChoiceItems(participantNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("Calculate Split", (dialog, which) -> {
            int selectedCount = 0;
            for (boolean checked : checkedItems) {
                if (checked) selectedCount++;
            }
            if (selectedCount > 0) {
                processSplitBill(selectedCount);
            } else {
                Toast.makeText(ScreenBill.this, "No participants selected.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showManualSplitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Split Bill");
        builder.setMessage("How many people are sharing this bill?");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Calculate", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int numOfPeople = Integer.parseInt(val);
                if (numOfPeople > 0) {
                    processSplitBill(numOfPeople);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void processSplitBill(int numberOfPeople) {
        com.studdict.mobile.model.SplitRequest req = new com.studdict.mobile.model.SplitRequest(currentBillId, numberOfPeople);
        ApiClient.getApi().splitBill(req).enqueue(new Callback<com.studdict.mobile.model.SplitResponse>() {
            @Override
            public void onResponse(Call<com.studdict.mobile.model.SplitResponse> call, Response<com.studdict.mobile.model.SplitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double amountPerPerson = response.body().getAmountPerPerson();
                    Intent intent = new Intent(ScreenBill.this, ScreenPayment.class);
                    intent.putExtra("BILL_ID", currentBillId);
                    intent.putExtra("AMOUNT", amountPerPerson);
                    intent.putExtra("STUDENT_ID", studentId);
                    intent.putExtra("RESERVATION_ID", reservationId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ScreenBill.this, "Failed to split bill.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<com.studdict.mobile.model.SplitResponse> call, Throwable t) {
                Toast.makeText(ScreenBill.this, "Network error splitting bill.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void payWithCash() {
        if (currentBillId == null) return;
        Toast.makeText(this, "Please wait for staff to collect cash...", Toast.LENGTH_SHORT).show();
        
        com.studdict.mobile.model.PaymentRequest req = new com.studdict.mobile.model.PaymentRequest(currentBillId, "CASH", currentTotalAmount);
        ApiClient.getApi().processPayment(req).enqueue(new Callback<com.studdict.mobile.model.PaymentResponse>() {
            @Override
            public void onResponse(Call<com.studdict.mobile.model.PaymentResponse> call, Response<com.studdict.mobile.model.PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    executeCheckoutPointsEarning();
                } else {
                    Toast.makeText(ScreenBill.this, "Cash payment failed.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<com.studdict.mobile.model.PaymentResponse> call, Throwable t) {
                Toast.makeText(ScreenBill.this, "Mocking cash payment success due to offline...", Toast.LENGTH_SHORT).show();
                executeCheckoutPointsEarning();
            }
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_congratulations, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button button = dialogView.findViewById(R.id.dialogButton);

        title.setText("⭐ Congratulations! ⭐");
        message.setText("You earned " + pointsEarned + " points for your study session!\n\nNew wallet balance: " + newBalance + " points.");
        button.setText("Awesome!");

        button.setOnClickListener(v -> {
            dialog.dismiss();
            completeCheckout();
        });

        dialog.setCancelable(false);
        dialog.show();
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
