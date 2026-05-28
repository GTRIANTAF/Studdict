package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.StudyTable;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenTables extends Activity {

    private long venueId;
    private String venueName, date, time, subject;
    private int duration, capacity;
    private boolean isPublic;

    private LinearLayout tablesContainer;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_tables);

        Intent i = getIntent();
        venueId = i.getLongExtra("VENUE_ID", 1L);
        venueName = i.getStringExtra("VENUE_NAME");
        date = i.getStringExtra("DATE");
        time = i.getStringExtra("TIME");
        duration = i.getIntExtra("DURATION", 120);
        capacity = i.getIntExtra("CAPACITY", 1);
        isPublic = i.getBooleanExtra("IS_PUBLIC", false);
        subject = i.getStringExtra("SUBJECT");

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(venueName);

        tablesContainer = findViewById(R.id.tablesContainer);
        statusText = findViewById(R.id.statusText);

        findViewById(R.id.backButton).setOnClickListener(view -> finish());

        fetchAvailableTables();

        android.view.View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, ScreenVenues.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }

    private void fetchAvailableTables() {
        if (isPublic && subject != null && !subject.trim().isEmpty()) {
            ApiClient.getApi().getMatchmakingTables(venueId, subject).enqueue(new Callback<List<StudyTable>>() {
                @Override
                public void onResponse(Call<List<StudyTable>> call, Response<List<StudyTable>> response) {
                    handleResponse(response);
                }
                @Override
                public void onFailure(Call<List<StudyTable>> call, Throwable t) {
                    statusText.setText("Network error: " + t.getMessage());
                }
            });
        } else {
            ApiClient.getApi().getAvailableTables(venueId, date, time, duration, capacity).enqueue(new Callback<List<StudyTable>>() {
                @Override
                public void onResponse(Call<List<StudyTable>> call, Response<List<StudyTable>> response) {
                    handleResponse(response);
                }
                @Override
                public void onFailure(Call<List<StudyTable>> call, Throwable t) {
                    statusText.setText("Network error: " + t.getMessage());
                }
            });
        }
    }

    private void handleResponse(Response<List<StudyTable>> response) {
        tablesContainer.removeAllViews();
        if (response.isSuccessful() && response.body() != null) {
            List<StudyTable> tables = response.body();
            if (tables.isEmpty()) {
                statusText.setText("No tables available for your criteria.");
            } else {
                statusText.setText("Found " + tables.size() + " available tables.");
                for (StudyTable table : tables) {
                    addTableToUI(table);
                }
            }
        } else {
            statusText.setText("Error: Server returned " + response.code());
        }
    }

    private void addTableToUI(StudyTable table) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.table_result);
        row.setPadding(32, 32, 32, 32);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        row.setLayoutParams(params);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView nameText = new TextView(this);
        nameText.setText("Table " + table.getTableNumber());
        nameText.setTextSize(18f);
        nameText.setTextColor(Color.parseColor("#111111"));
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView capText = new TextView(this);
        capText.setText("Capacity: " + table.getCapacity() + " people");
        capText.setTextSize(14f);
        capText.setTextColor(Color.parseColor("#777777"));

        textCol.addView(nameText);
        textCol.addView(capText);

        TextView icon = new TextView(this);
        icon.setText(">");
        icon.setTextSize(24f);
        icon.setTextColor(Color.parseColor("#777777"));

        row.addView(textCol);
        row.addView(icon);

        row.setOnClickListener(v -> proceedToConfirm(table));
        tablesContainer.addView(row);
    }

    private void proceedToConfirm(StudyTable table) {
        Intent intent = new Intent(this, ScreenConfirm.class);
        intent.putExtra("VENUE_ID", venueId);
        intent.putExtra("VENUE_NAME", venueName);
        intent.putExtra("DATE", date);
        intent.putExtra("TIME", time);
        intent.putExtra("DURATION", duration);
        intent.putExtra("CAPACITY", capacity);
        intent.putExtra("IS_PUBLIC", isPublic);
        intent.putExtra("SUBJECT", subject);
        intent.putExtra("TABLE_ID", table.getId());
        intent.putExtra("TABLE_NUMBER", table.getTableNumber());
        startActivity(intent);
    }
}
