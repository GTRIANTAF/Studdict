package com.studdict.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.studdict.mobile.model.EBookLoanInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenEBookVault extends Activity {

    private RecyclerView recyclerBooks;
    private EBookAdapter adapter;
    private EditText editSearch;
    private long checkInId;

    private List<EBook> currentBooks = new ArrayList<>();
    private final Map<Long, Long> myLoansByBookId = new HashMap<>(); // bookId → loanId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkInId = new SessionManager(this).getCheckInId();
        if (checkInId == -1L) {
            Toast.makeText(this, "You must be checked in to access the E-Book Vault.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_ebook_vault);

        editSearch = findViewById(R.id.editSearchKeyword);
        Button btnSearch = findViewById(R.id.btnSearch);
        recyclerBooks = findViewById(R.id.recyclerBooks);
        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EBookAdapter(new ArrayList<>(), new HashMap<>(), this::onLoanClicked, this::onReturnClicked);
        recyclerBooks.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> loadCatalog(editSearch.getText().toString().trim()));

        verifyCheckIn();
        loadCatalog("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loan state when returning from the reader
        loadActiveLoans(() -> adapter.setData(currentBooks, myLoansByBookId));
    }

    private void verifyCheckIn() {
        ApiClient.getApi().requestAccess(checkInId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful() || Boolean.FALSE.equals(response.body())) {
                    Toast.makeText(ScreenEBookVault.this, "Check-in required to access the E-Book Vault.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                // Offline — allow access in demo mode
            }
        });
    }

    private void loadCatalog(String keyword) {
        ApiClient.getApi().getCatalog(keyword).enqueue(new Callback<List<EBook>>() {
            @Override
            public void onResponse(Call<List<EBook>> call, Response<List<EBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBooks = response.body();
                } else {
                    currentBooks = getDummyBooks(keyword);
                    Toast.makeText(ScreenEBookVault.this, "Offline: showing demo books.", Toast.LENGTH_SHORT).show();
                }
                loadActiveLoans(() -> adapter.setData(currentBooks, myLoansByBookId));
            }

            @Override
            public void onFailure(Call<List<EBook>> call, Throwable t) {
                currentBooks = getDummyBooks(keyword);
                Toast.makeText(ScreenEBookVault.this, "Offline: showing demo books.", Toast.LENGTH_SHORT).show();
                loadActiveLoans(() -> adapter.setData(currentBooks, myLoansByBookId));
            }
        });
    }

    private void loadActiveLoans(Runnable onDone) {
        ApiClient.getApi().getActiveLoans(checkInId).enqueue(new Callback<List<EBookLoanInfo>>() {
            @Override
            public void onResponse(Call<List<EBookLoanInfo>> call, Response<List<EBookLoanInfo>> response) {
                myLoansByBookId.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (EBookLoanInfo info : response.body()) {
                        if (info.getEbookId() != null && info.getLoanId() != null) {
                            myLoansByBookId.put(info.getEbookId(), info.getLoanId());
                        }
                    }
                }
                runOnUiThread(onDone);
            }

            @Override
            public void onFailure(Call<List<EBookLoanInfo>> call, Throwable t) {
                runOnUiThread(onDone);
            }
        });
    }

    private void onLoanClicked(EBook book) {
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
        ApiClient.getApi().requestLoan(checkInId, book.getId()).enqueue(new Callback<EBookLoan>() {
            @Override
            public void onResponse(Call<EBookLoan> call, Response<EBookLoan> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long loanId = response.body().getLoanId();
                    myLoansByBookId.put(book.getId(), loanId);
                    adapter.setData(currentBooks, myLoansByBookId);
                    openReader(loanId, book);
                } else if (response.code() == 403) {
                    Toast.makeText(ScreenEBookVault.this, "Check-in is no longer valid. Please check in again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ScreenEBookVault.this, "No license available for this book.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EBookLoan> call, Throwable t) {
                Toast.makeText(ScreenEBookVault.this, "Offline demo: opening book.", Toast.LENGTH_SHORT).show();
                openReader(-1L, book);
            }
        });
    }

    private void onReturnClicked(EBook book, long loanId) {
        new AlertDialog.Builder(this)
                .setTitle("Return E-Book")
                .setMessage("Are you sure you want to return \"" + book.getTitle() + "\"?")
                .setPositiveButton("Return", (dialog, which) -> executeReturn(book, loanId))
                .setNegativeButton("Keep Reading", null)
                .show();
    }

    private void executeReturn(EBook book, long loanId) {
        ApiClient.getApi().requestReturn(loanId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                myLoansByBookId.remove(book.getId());
                adapter.setData(currentBooks, myLoansByBookId);
                Toast.makeText(ScreenEBookVault.this, "\"" + book.getTitle() + "\" returned.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                myLoansByBookId.remove(book.getId());
                adapter.setData(currentBooks, myLoansByBookId);
                Toast.makeText(ScreenEBookVault.this, "Book returned (offline).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openReader(long loanId, EBook book) {
        String content = book.getContent() != null ? book.getContent()
                : "Demo excerpt of \"" + book.getTitle() + "\" by " + book.getAuthor()
                + ".\n\n[Connect to backend for full text.]";
        Intent intent = new Intent(this, ScreenEBookReader.class);
        intent.putExtra("LOAN_ID", loanId);
        intent.putExtra("BOOK_ID", book.getId() != null ? book.getId() : -1L);
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
                "Chapter 1: Clean Code always looks like it was written by someone who cares..."},
            {"3", "Design Patterns", "Gamma, Helm, Johnson, Vlissides",
                "Chapter 1: Introduction — Designing reusable object-oriented software is hard..."},
            {"4", "Database System Concepts", "Silberschatz, Korth, Sudarshan",
                "Chapter 1: Introduction — A DBMS is a collection of interrelated data..."},
            {"5", "Operating System Concepts", "Silberschatz, Galvin, Gagne",
                "Chapter 1: An operating system manages a computer's hardware..."},
            {"6", "Computer Networks", "Andrew S. Tanenbaum",
                "Chapter 1: A computer network is an interconnected collection of autonomous computers..."},
            {"7", "Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig",
                "Chapter 1: AI attempts not just to understand but also to build intelligent entities..."},
            {"8", "The Pragmatic Programmer", "David Thomas, Andrew Hunt",
                "Chapter 1: Pragmatic programmers are not wedded to any particular technology..."},
            {"9", "Calculus: Early Transcendentals", "James Stewart",
                "Chapter 1: Functions and Models — The fundamental objects we deal with in calculus are functions..."},
            {"10", "Linear Algebra and Its Applications", "Gilbert Strang",
                "Chapter 1: The heart of linear algebra is in two operations — both with vectors..."},
            {"11", "Discrete Mathematics and Its Applications", "Kenneth H. Rosen",
                "Chapter 1: The rules of logic specify the precise meanings of mathematical statements..."},
            {"12", "University Physics", "Young, Freedman",
                "Chapter 1: Physics is one of the most fundamental of the sciences..."},
            {"13", "Principles of Economics", "N. Gregory Mankiw",
                "Chapter 1: Ten Principles of Economics — The word economy comes from the Greek oikonomos..."},
        };
        for (String[] d : data) {
            EBook book = new EBook();
            book.setId(Long.parseLong(d[0]));
            book.setTitle(d[1]);
            book.setAuthor(d[2]);
            // Pages separated by form-feed ('\f') so the reader can paginate offline too.
            book.setContent(d[3]
                    + "\fPage 2\n\nThis is a second demo page so the reader's page navigation "
                    + "can be tried offline. Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
                    + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
                    + "\fPage 3\n\nConnect to the backend for the full digital text of this title.\n\n"
                    + "(End of demo sample)");
            book.setAvailable(true);
            all.add(book);
        }
        if (keyword == null || keyword.isEmpty()) return all;
        String kw = keyword.toLowerCase();
        List<EBook> filtered = new ArrayList<>();
        for (EBook b : all) {
            if (b.getTitle().toLowerCase().contains(kw) || b.getAuthor().toLowerCase().contains(kw)) {
                filtered.add(b);
            }
        }
        return filtered.isEmpty() ? all : filtered;
    }

    // --- Adapter ---

    interface OnLoanClickListener {
        void onLoan(EBook book);
    }

    interface OnReturnClickListener {
        void onReturn(EBook book, long loanId);
    }

    private static class EBookAdapter extends RecyclerView.Adapter<EBookAdapter.ViewHolder> {

        private List<EBook> books;
        private Map<Long, Long> myLoans; // bookId → loanId
        private final OnLoanClickListener loanListener;
        private final OnReturnClickListener returnListener;

        EBookAdapter(List<EBook> books, Map<Long, Long> myLoans,
                     OnLoanClickListener loanListener, OnReturnClickListener returnListener) {
            this.books = books;
            this.myLoans = myLoans;
            this.loanListener = loanListener;
            this.returnListener = returnListener;
        }

        void setData(List<EBook> books, Map<Long, Long> myLoans) {
            this.books = new ArrayList<>(books);
            this.myLoans = new HashMap<>(myLoans);
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

            Long loanId = myLoans.get(book.getId());
            if (loanId != null) {
                // Book is currently loaned by this user
                holder.txtStatus.setText("Loaned by you");
                holder.txtStatus.setTextColor(Color.parseColor("#E65100"));
                holder.btnAction.setText("Return");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FF6D00")));
                holder.btnAction.setOnClickListener(v -> returnListener.onReturn(book, loanId));
            } else if (book.isAvailable()) {
                holder.txtStatus.setText("Available");
                holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.btnAction.setText("Loan");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#A855F7")));
                holder.btnAction.setOnClickListener(v -> loanListener.onLoan(book));
            } else {
                holder.txtStatus.setText("Not available");
                holder.txtStatus.setTextColor(Color.parseColor("#B71C1C"));
                holder.btnAction.setText("Not available");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#BDBDBD")));
                holder.btnAction.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return books != null ? books.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle, txtAuthor, txtStatus;
            Button btnAction;

            ViewHolder(View v) {
                super(v);
                txtTitle = v.findViewById(R.id.txtBookTitle);
                txtAuthor = v.findViewById(R.id.txtBookAuthor);
                txtStatus = v.findViewById(R.id.txtBookStatus);
                btnAction = v.findViewById(R.id.btnBookAction);
            }
        }
    }
}
