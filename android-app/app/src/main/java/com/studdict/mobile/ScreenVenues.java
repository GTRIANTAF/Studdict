package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScreenVenues extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        String studentName = getIntent().getStringExtra("STUDENT_NAME");
        TextView greetingText = findViewById(R.id.greetingText);
        if (studentName != null && !studentName.isEmpty() && greetingText != null) {
            greetingText.setText("Hello, " + studentName + "! 👋");
        }

        LinearLayout venueCeid = findViewById(R.id.venueCeid);
        LinearLayout venueCafe = findViewById(R.id.venueCafe);

        venueCeid.setOnClickListener(v -> selectVenue(1L, "CEID LIBRARY"));
        venueCafe.setOnClickListener(v -> selectVenue(2L, "PATRAS CITY CAFE"));

        View navLiveBoard = findViewById(R.id.navLiveBoard);
        navLiveBoard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenLiveBoard.class);
            startActivity(intent);
        });
    }

    private void selectVenue(long venueId, String venueName) {
        Intent intent = new Intent(this, ScreenForm.class);
        intent.putExtra("VENUE_ID", venueId);
        intent.putExtra("VENUE_NAME", venueName);
        startActivity(intent);
    }
}
