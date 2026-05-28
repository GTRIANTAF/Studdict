package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.Bill;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UC8 Gap 9 — BillScreen: refreshScreen().
 * Shown automatically after a successful order, displays the current bill for the table.
 */
public class ScreenBill extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        int tableId = getIntent().getIntExtra("TABLE_ID", 1);

        TextView txtBillId = findViewById(R.id.txtBillId);
        TextView txtBillTable = findViewById(R.id.txtBillTable);
        TextView txtBillTotal = findViewById(R.id.txtBillTotal);
        TextView txtBillStatus = findViewById(R.id.txtBillStatus);
        Button btnClose = findViewById(R.id.btnCloseBill);
        Button btnCheckout = findViewById(R.id.btnCheckout);

        txtBillTable.setText("Table: " + tableId);
        btnClose.setOnClickListener(v -> finish());
        
        btnCheckout.setEnabled(false);

        ApiClient.getApi().getBillByTable(tableId).enqueue(new Callback<Bill>() {
            @Override
            public void onResponse(Call<Bill> call, Response<Bill> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bill bill = response.body();
                    txtBillId.setText("Bill #" + bill.getBillId());
                    txtBillTotal.setText(String.format("Total: €%.2f", bill.getTotalAmount()));
                    txtBillStatus.setText(bill.isSettled() ? "Status: Paid" : "Status: Pending payment");
                    
                    if (!bill.isSettled()) {
                        btnCheckout.setEnabled(true);
                        btnCheckout.setOnClickListener(v -> {
                            Intent intent = new Intent(ScreenBill.this, ScreenPayment.class);
                            intent.putExtra("BILL_ID", bill.getBillId());
                            intent.putExtra("TOTAL_AMOUNT", bill.getTotalAmount());
                            startActivity(intent);
                            finish();
                        });
                    }
                } else {
                    txtBillId.setText("Bill");
                    txtBillTotal.setText("Total: —");
                    txtBillStatus.setText("Bill not found.");
                }
            }

            @Override
            public void onFailure(Call<Bill> call, Throwable t) {
                // Demo mode: show placeholder
                txtBillId.setText("Demo Bill");
                txtBillTotal.setText("Total: see receipt");
                txtBillStatus.setText("Status: Pending payment");
                Toast.makeText(ScreenBill.this, "Offline: bill details unavailable.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
