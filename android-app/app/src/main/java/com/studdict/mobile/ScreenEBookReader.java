package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.EBookLoan;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenEBookReader extends Activity {

    private long loanId;
    private Handler pollHandler;
    private static final long POLL_INTERVAL_MS = 10_000L;

    // UC7: polls every 10s to detect external loan termination
    // (Standard Check-out branch and Automatic Revocation branch)
    private final Runnable pollLoanStatus = new Runnable() {
        @Override
        public void run() {
            ApiClient.getApi().getLoanStatus(loanId).enqueue(new Callback<EBookLoan>() {
                @Override
                public void onResponse(Call<EBookLoan> call, Response<EBookLoan> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!response.body().isActive()) {
                            onLoanTerminatedExternally();
                        } else {
                            pollHandler.postDelayed(pollLoanStatus, POLL_INTERVAL_MS);
                        }
                    } else {
                        pollHandler.postDelayed(pollLoanStatus, POLL_INTERVAL_MS);
                    }
                }

                @Override
                public void onFailure(Call<EBookLoan> call, Throwable t) {
                    pollHandler.postDelayed(pollLoanStatus, POLL_INTERVAL_MS);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_reader);

        loanId = getIntent().getLongExtra("LOAN_ID", -1L);
        String title = getIntent().getStringExtra("BOOK_TITLE");
        String content = getIntent().getStringExtra("BOOK_CONTENT");

        TextView txtTitle = findViewById(R.id.txtReaderTitle);
        TextView txtContent = findViewById(R.id.txtReaderContent);
        Button btnReturn = findViewById(R.id.btnReturnBook);

        txtTitle.setText(title);
        txtContent.setText(content);

        // UC7 Early Return: ReturnConfirmScreen shown before releaseLoan is called
        btnReturn.setOnClickListener(v -> showReturnConfirmation());

        pollHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only poll for real loans; loanId <= 0 means demo/offline mode
        if (loanId > 0) {
            pollHandler.postDelayed(pollLoanStatus, POLL_INTERVAL_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollLoanStatus);
    }

    private void showReturnConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Return E-Book")
                .setMessage("Are you sure you want to return this book?")
                .setPositiveButton("Return", (dialog, which) -> returnBook())
                .setNegativeButton("Keep Reading", null)
                .show();
    }

    private void returnBook() {
        if (loanId <= 0) {
            Toast.makeText(this, "Returned Successfully", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ApiClient.getApi().requestReturn(loanId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    pollHandler.removeCallbacks(pollLoanStatus);
                    Toast.makeText(ScreenEBookReader.this, "Returned Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ScreenEBookReader.this, "Failed to return.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ScreenEBookReader.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // UC7 Gap 4: expiry shows "Reservation Expired" and navigates to MainMenu (ScreenVenues)
    private void onLoanTerminatedExternally() {
        pollHandler.removeCallbacks(pollLoanStatus);
        Toast.makeText(this, "Reservation Expired", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ScreenVenues.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
