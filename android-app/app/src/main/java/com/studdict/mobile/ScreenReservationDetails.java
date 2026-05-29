package com.studdict.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.GenerateInviteCodeRequest;
import com.studdict.mobile.model.InviteCode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenReservationDetails extends Activity {

    private long reservationId;
    private long tableId;
    private int tableNumber;
    private String venueName;
    private String date;
    private String time;
    private int duration;
    private int capacity;
    private String studentId;

    private TextView inviteCodeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reservationId = getIntent().getLongExtra("RESERVATION_ID", -1L);
        tableId = getIntent().getLongExtra("TABLE_ID", -1L);
        tableNumber = getIntent().getIntExtra("TABLE_NUMBER", 1);
        venueName = getIntent().getStringExtra("VENUE_NAME");
        date = getIntent().getStringExtra("DATE");
        time = getIntent().getStringExtra("TIME");
        duration = getIntent().getIntExtra("DURATION", 120);
        capacity = getIntent().getIntExtra("CAPACITY", 1);
        studentId = getIntent().getStringExtra("STUDENT_ID");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 64, 48, 48);
        root.setBackgroundColor(Color.WHITE);

        TextView back = new TextView(this);
        back.setText("<");
        back.setTextSize(42);
        back.setTextColor(Color.BLACK);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = new TextView(this);
        title.setText("Reservation Details");
        title.setTextSize(30);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.START);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView details = new TextView(this);
        details.setText(
                "\nReservation ID: " + reservationId +
                        "\nVenue: " + venueName +
                        "\nTable: " + tableNumber +
                        "\nDate: " + date +
                        "\nTime: " + time +
                        "\nDuration: " + (duration / 60) + " hours" +
                        "\nCapacity: " + capacity + " people"
        );
        details.setTextSize(18);
        details.setTextColor(Color.DKGRAY);
        details.setPadding(0, 24, 0, 32);
        root.addView(details);

        inviteCodeText = new TextView(this);
        inviteCodeText.setText("Invite Code: -");
        inviteCodeText.setTextSize(22);
        inviteCodeText.setTextColor(Color.parseColor("#6E55DC"));
        inviteCodeText.setTypeface(null, android.graphics.Typeface.BOLD);
        inviteCodeText.setPadding(0, 8, 0, 24);
        root.addView(inviteCodeText);

        Button generateInviteButton = new Button(this);
        generateInviteButton.setText("Generate Invite Code");
        generateInviteButton.setAllCaps(false);
        generateInviteButton.setTextSize(18);
        generateInviteButton.setTextColor(Color.parseColor("#6E55DC"));
        generateInviteButton.setBackgroundResource(R.drawable.button_invite_soft);
        LinearLayout.LayoutParams inviteButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inviteButtonParams.setMargins(0, 8, 0, 0);
        generateInviteButton.setLayoutParams(inviteButtonParams);
        generateInviteButton.setOnClickListener(v -> generateInviteCode());
        root.addView(generateInviteButton);

        setContentView(root);
    }

    private void generateInviteCode() {
        if (reservationId <= 0 || studentId == null || studentId.isBlank()) {
            Toast.makeText(
                    this,
                    "Missing reservation or student data.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        GenerateInviteCodeRequest request =
                new GenerateInviteCodeRequest(reservationId, studentId);

        ApiClient.getApi().generateInviteCode(request).enqueue(new Callback<InviteCode>() {
            @Override
            public void onResponse(Call<InviteCode> call, Response<InviteCode> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String code = response.body().getCode();

                    inviteCodeText.setText("Invite Code: " + code);

                    Toast.makeText(
                            ScreenReservationDetails.this,
                            "Invite code generated successfully.",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(
                            ScreenReservationDetails.this,
                            "Could not generate invite code. HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<InviteCode> call, Throwable t) {
                Toast.makeText(
                        ScreenReservationDetails.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
