package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScreenCheckInSuccess extends Activity {

    private static final int PURPLE = Color.parseColor("#A855F7");
    private static final int PURPLE_DARK = Color.parseColor("#7E22CE");
    private static final int TEXT = Color.parseColor("#111827");
    private static final int MUTED = Color.parseColor("#4B5563");
    private static final int BORDER = Color.parseColor("#E9D5FF");
    private static final int BACKGROUND = Color.parseColor("#FBF7FF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);
        int checkedInCount = getIntent().getIntExtra("CHECKED_IN_COUNT", 0);
        String studentId = getIntent().getStringExtra("STUDENT_ID");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(40, 96, 40, 48);
        root.setBackgroundColor(BACKGROUND);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(32, 40, 32, 40);
        card.setBackground(roundedBackground(Color.WHITE, BORDER, 32, 2));
        root.addView(card, matchWidthWithBottomMargin(0));

        TextView icon = new TextView(this);
        icon.setText("\u2713");
        icon.setTextSize(42);
        icon.setTextColor(Color.WHITE);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(roundedBackground(PURPLE, PURPLE, 999, 0));
        card.addView(icon, fixedBox(88, 88, 0, 0, 0, 24));

        TextView title = new TextView(this);
        title.setText("Check-in successful");
        title.setTextSize(30);
        title.setTextColor(Color.BLACK);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 14);
        card.addView(title);

        TextView reservation = new TextView(this);
        reservation.setText("Reservation #" + reservationId);
        reservation.setTextSize(19);
        reservation.setTextColor(PURPLE_DARK);
        reservation.setTypeface(null, Typeface.BOLD);
        reservation.setGravity(Gravity.CENTER);
        reservation.setPadding(0, 0, 0, 26);
        card.addView(reservation);

        TextView message = new TextView(this);
        message.setText(
                checkedInCount + " participant(s) were marked as checked-in.\n\n" +
                "Study services are now active for the checked-in participant(s)."
        );
        message.setTextSize(18);
        message.setTextColor(MUTED);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, 34);
        card.addView(message);

        Button myBookingsButton = new Button(this);
        myBookingsButton.setText("Back to My Bookings");
        myBookingsButton.setAllCaps(false);
        myBookingsButton.setTextSize(16);
        myBookingsButton.setTextColor(Color.WHITE);
        myBookingsButton.setTypeface(null, Typeface.BOLD);
        myBookingsButton.setPadding(16, 18, 16, 18);
        myBookingsButton.setBackground(roundedBackground(PURPLE, PURPLE, 20, 0));
        myBookingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ScreenCheckInSuccess.this, ScreenMyBookings.class);
            intent.putExtra("STUDENT_ID", studentId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        card.addView(myBookingsButton, matchWidthWithBottomMargin(0));

        setContentView(root);
    }

    private GradientDrawable roundedBackground(int fillColor, int strokeColor, int radiusDp, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private LinearLayout.LayoutParams matchWidthWithBottomMargin(int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(bottomMarginDp));
        return params;
    }

    private LinearLayout.LayoutParams fixedBox(int widthDp, int heightDp, int leftDp, int topDp, int rightDp, int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(widthDp), dp(heightDp));
        params.setMargins(dp(leftDp), dp(topDp), dp(rightDp), dp(bottomDp));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
