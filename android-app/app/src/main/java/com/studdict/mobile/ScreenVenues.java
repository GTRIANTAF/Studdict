package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ScreenVenues extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venues);

        LinearLayout venueCeid = findViewById(R.id.venueCeid);
        LinearLayout venueCafe = findViewById(R.id.venueCafe);

        venueCeid.setOnClickListener(v -> selectVenue(1L, "CEID LIBRARY"));
        venueCafe.setOnClickListener(v -> selectVenue(2L, "PATRAS CITY CAFE"));
    }

    private void selectVenue(long venueId, String venueName) {
        Intent intent = new Intent(this, ScreenForm.class);
        intent.putExtra("VENUE_ID", venueId);
        intent.putExtra("VENUE_NAME", venueName);
        startActivity(intent);
    }
}
