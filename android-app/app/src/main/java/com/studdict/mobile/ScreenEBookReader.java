package com.studdict.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenEBookReader extends Activity {

    private long loanId;

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

        btnReturn.setOnClickListener(v -> returnBook());
    }

    private void returnBook() {
        ApiClient.getApi().requestReturn(loanId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ScreenEBookReader.this, "Returned Successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to vault
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
}
