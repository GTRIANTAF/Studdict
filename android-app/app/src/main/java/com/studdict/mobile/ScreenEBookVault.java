package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.EBook;
import com.studdict.mobile.model.EBookLoan;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenEBookVault extends Activity {

    private RecyclerView recyclerBooks;
    private EBookAdapter adapter;
    private long mockCheckInId = 1L; // Mock check-in ID for demonstration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_vault);

        EditText editSearch = findViewById(R.id.editSearchKeyword);
        Button btnSearch = findViewById(R.id.btnSearch);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EBookAdapter(new ArrayList<>(), this::requestLoan);
        recyclerBooks.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> performSearch(editSearch.getText().toString()));
    }

    private void performSearch(String keyword) {
        ApiClient.getApi().executeSearch(keyword).enqueue(new Callback<List<EBook>>() {
            @Override
            public void onResponse(Call<List<EBook>> call, Response<List<EBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setBooks(response.body());
                } else {
                    Toast.makeText(ScreenEBookVault.this, "Search failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EBook>> call, Throwable t) {
                Toast.makeText(ScreenEBookVault.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLoan(EBook book) {
        ApiClient.getApi().requestLoan(mockCheckInId, book.getId()).enqueue(new Callback<EBookLoan>() {
            @Override
            public void onResponse(Call<EBookLoan> call, Response<EBookLoan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ScreenEBookVault.this, "Loan Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ScreenEBookVault.this, ScreenEBookReader.class);
                    intent.putExtra("LOAN_ID", response.body().getLoanId());
                    intent.putExtra("BOOK_TITLE", book.getTitle());
                    intent.putExtra("BOOK_CONTENT", book.getContent());
                    startActivity(intent);
                } else {
                    Toast.makeText(ScreenEBookVault.this, "Not available or Check-In invalid.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EBookLoan> call, Throwable t) {
                Toast.makeText(ScreenEBookVault.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Adapter ---
    private static class EBookAdapter extends RecyclerView.Adapter<EBookAdapter.ViewHolder> {
        private List<EBook> books;
        private final OnBookClickListener listener;

        interface OnBookClickListener {
            void onBookClick(EBook book);
        }

        EBookAdapter(List<EBook> books, OnBookClickListener listener) {
            this.books = books;
            this.listener = listener;
        }

        void setBooks(List<EBook> books) {
            this.books = books;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ebook, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EBook book = books.get(position);
            holder.txtTitle.setText(book.getTitle());
            holder.txtAuthor.setText(book.getAuthor());
            holder.itemView.setOnClickListener(v -> listener.onBookClick(book));
        }

        @Override
        public int getItemCount() {
            return books != null ? books.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle, txtAuthor;
            ViewHolder(View v) {
                super(v);
                txtTitle = v.findViewById(R.id.txtBookTitle);
                txtAuthor = v.findViewById(R.id.txtBookAuthor);
            }
        }
    }
}
