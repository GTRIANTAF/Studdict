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
    private long mockCheckInId = 1L; // Replaced by scanner check-in ID when UC5 is integrated

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

        verifyCheckIn();
    }

    // UC7: Verify the student has an active check-in before granting vault access
    private void verifyCheckIn() {
        ApiClient.getApi().requestAccess(mockCheckInId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful() || Boolean.FALSE.equals(response.body())) {
                    Toast.makeText(ScreenEBookVault.this, "Check-in required to access the E-Book Vault.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(ScreenEBookVault.this, "Offline mode: demo books available.", Toast.LENGTH_SHORT).show();
            }
        });
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
                adapter.setBooks(getDummyBooks(keyword));
                Toast.makeText(ScreenEBookVault.this, "Offline: showing demo results.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // UC7 Gap 1: checkAvailability is a separate client call before requestLoan
    private void requestLoan(EBook book) {
        ApiClient.getApi().checkEBookAvailability(book.getId()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    createLoan(book);
                } else {
                    Toast.makeText(ScreenEBookVault.this, "No license available for this book.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                createLoan(book); // Demo mode: proceed
            }
        });
    }

    private void createLoan(EBook book) {
        ApiClient.getApi().requestLoan(mockCheckInId, book.getId()).enqueue(new Callback<EBookLoan>() {
            @Override
            public void onResponse(Call<EBookLoan> call, Response<EBookLoan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    openReader(response.body().getLoanId(), book);
                } else if (response.code() == 403) {
                    Toast.makeText(ScreenEBookVault.this, "Check-in is no longer valid. Please check in again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ScreenEBookVault.this, "No license available for this book.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EBookLoan> call, Throwable t) {
                // Demo mode: pass -1L so ScreenEBookReader skips loan-status polling
                Toast.makeText(ScreenEBookVault.this, "Offline demo: opening book.", Toast.LENGTH_SHORT).show();
                openReader(-1L, book);
            }
        });
    }

    private void openReader(long loanId, EBook book) {
        String content = book.getContent() != null ? book.getContent()
                : "Demo excerpt of \"" + book.getTitle() + "\" by " + book.getAuthor()
                + ".\n\n[Connect to backend for full text.]";
        Intent intent = new Intent(this, ScreenEBookReader.class);
        intent.putExtra("LOAN_ID", loanId);
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_CONTENT", content);
        startActivity(intent);
    }

    private List<EBook> getDummyBooks(String keyword) {
        List<EBook> all = new ArrayList<>();
        String[][] data = {
            {"1", "Introduction to Algorithms", "Cormen, Leiserson, Rivest, Stein",
                "Chapter 1: The Role of Algorithms in Computing\n\nAn algorithm is any well-defined computational procedure..."},
            {"2", "Clean Code", "Robert C. Martin",
                "Chapter 1: Clean Code\n\nThere are two things about software — its beauty and its mess..."},
            {"3", "Design Patterns", "Gang of Four",
                "Chapter 1: Introduction\n\nDesigning object-oriented software is hard..."},
            {"4", "Database System Concepts", "Silberschatz, Korth, Sudarshan",
                "Chapter 1: Introduction\n\nA database-management system (DBMS) is a collection of interrelated data..."},
            {"5", "Operating System Concepts", "Silberschatz, Galvin, Gagne",
                "Chapter 1: Introduction\n\nAn operating system is a program that manages a computer's hardware..."},
        };
        for (String[] d : data) {
            EBook book = new EBook();
            book.setId(Long.parseLong(d[0]));
            book.setTitle(d[1]);
            book.setAuthor(d[2]);
            book.setContent(d[3] + "\n\n[Demo content — connect to backend for full text.]");
            all.add(book);
        }
        if (keyword.isEmpty()) return all;
        String kw = keyword.toLowerCase();
        List<EBook> filtered = new ArrayList<>();
        for (EBook b : all) {
            if (b.getTitle().toLowerCase().contains(kw) || b.getAuthor().toLowerCase().contains(kw))
                filtered.add(b);
        }
        return filtered.isEmpty() ? all : filtered;
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
