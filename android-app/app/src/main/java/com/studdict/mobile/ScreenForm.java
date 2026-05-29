package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.InviteJoinResponse;
import com.studdict.mobile.model.JoinInviteCodeRequest;
import com.studdict.mobile.model.ValidateInviteCodeRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenForm extends Activity {

    private long selectedVenueId;
    private String venueName;

    private boolean publicMode = false;
    private int groupSize = 1;
    private String selectedDate;
    private String selectedTime = "10:00";
    private int selectedDuration = 120;

    private Button timeButton, durationButton;
    private Button todayButton, tomorrowButton, thirdDateButton;
    private TextView groupSizeText;
    private LinearLayout privateChoiceCard, publicChoiceCard;
    private EditText subjectInput, joinReservationInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_form);

        selectedVenueId = getIntent().getLongExtra("VENUE_ID", 1L);
        venueName = getIntent().getStringExtra("VENUE_NAME");
        if (venueName == null) venueName = "CEID LIBRARY";

        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText(venueName);
        }

        bindViews();
        setupDateChips();

        long resId = getIntent().getLongExtra("RESERVATION_ID", -1L);
        if (resId != -1L) {
            Button btnSearch = findViewById(R.id.searchButton);
            if (btnSearch != null) btnSearch.setText("Continue to Confirmation");
            if (privateChoiceCard != null) privateChoiceCard.setVisibility(View.GONE);
            if (publicChoiceCard != null) publicChoiceCard.setVisibility(View.GONE);
            if (joinReservationInput != null) joinReservationInput.setVisibility(View.GONE);
            View joinPublicBtn = findViewById(R.id.joinPublicButton);
            if (joinPublicBtn != null) joinPublicBtn.setVisibility(View.GONE);
            if (subjectInput != null) subjectInput.setVisibility(View.GONE);
            
            View groupSizeSection = findViewById(R.id.groupSizeSection);
            if (groupSizeSection != null) groupSizeSection.setVisibility(View.GONE);
        }

        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        findViewById(R.id.searchButton).setOnClickListener(view -> proceedToTables());
        findViewById(R.id.joinPublicButton).setOnClickListener(view -> joinPublicReservation());

        android.view.View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, ScreenVenues.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }
    }

    private void bindViews() {
        timeButton = findViewById(R.id.timeButton);
        durationButton = findViewById(R.id.durationButton);
        todayButton = findViewById(R.id.todayButton);
        tomorrowButton = findViewById(R.id.tomorrowButton);
        thirdDateButton = findViewById(R.id.thirdDateButton);
        groupSizeText = findViewById(R.id.groupSizeText);
        privateChoiceCard = findViewById(R.id.privateChoiceCard);
        publicChoiceCard = findViewById(R.id.publicChoiceCard);
        subjectInput = findViewById(R.id.subjectInput);
        joinReservationInput = findViewById(R.id.joinReservationInput);

        privateChoiceCard.setOnClickListener(v -> selectPrivateMode());
        publicChoiceCard.setOnClickListener(v -> selectPublicMode());

        findViewById(R.id.increaseGroupButton).setOnClickListener(v -> setGroupSize(groupSize + 1));
        findViewById(R.id.decreaseGroupButton).setOnClickListener(v -> setGroupSize(groupSize - 1));

        timeButton.setOnClickListener(view -> showTimePicker());
        durationButton.setOnClickListener(view -> showDurationPicker());
    }

    private void proceedToTables() {
        long resId = getIntent().getLongExtra("RESERVATION_ID", -1L);
        if (resId != -1L) {
            Intent intent = new Intent(this, ScreenConfirm.class);
            intent.putExtra("RESERVATION_ID", resId);
            intent.putExtra("NEW_DATE", selectedDate);
            intent.putExtra("NEW_TIME", selectedTime);
            intent.putExtra("NEW_DURATION", selectedDuration);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this, ScreenTables.class);
        intent.putExtra("VENUE_ID", selectedVenueId);
        intent.putExtra("VENUE_NAME", venueName);
        intent.putExtra("DATE", selectedDate);
        intent.putExtra("TIME", selectedTime);
        intent.putExtra("DURATION", selectedDuration);
        intent.putExtra("CAPACITY", groupSize);
        intent.putExtra("IS_PUBLIC", publicMode);
        if (publicMode) {
            intent.putExtra("SUBJECT", subjectInput.getText().toString());
        }
        startActivity(intent);
    }

    private void joinPublicReservation() {
        String inviteCode = joinReservationInput.getText().toString().trim();

        if (inviteCode.isEmpty()) {
            Toast.makeText(this, "Enter invite code", Toast.LENGTH_SHORT).show();
            return;
        }

        ValidateInviteCodeRequest request = new ValidateInviteCodeRequest(inviteCode);

        ApiClient.getApi().validateInviteCode(request).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    submitInviteJoin(inviteCode);
                } else {
                    Toast.makeText(
                            ScreenForm.this,
                            "Invalid or expired invite code.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(
                        ScreenForm.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void submitInviteJoin(String inviteCode) {
        JoinInviteCodeRequest request = new JoinInviteCodeRequest(inviteCode, "S2");

        ApiClient.getApi().joinReservationWithInviteCodeResult(request).enqueue(new Callback<InviteJoinResponse>() {
            @Override
            public void onResponse(Call<InviteJoinResponse> call, Response<InviteJoinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            ScreenForm.this,
                            response.body().getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    joinReservationInput.setText("");
                } else {
                    Toast.makeText(
                            ScreenForm.this,
                            "Could not join reservation. HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<InviteJoinResponse> call, Throwable t) {
                Toast.makeText(
                        ScreenForm.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setupDateChips() {
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar thirdDate = Calendar.getInstance();
        thirdDate.add(Calendar.DAY_OF_YEAR, 2);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFmt = new SimpleDateFormat("EEE, dd", Locale.US);

        thirdDateButton.setText(displayFmt.format(thirdDate.getTime()));

        String todayStr = fmt.format(today.getTime());
        String tomorrowStr = fmt.format(tomorrow.getTime());
        String thirdStr = fmt.format(thirdDate.getTime());

        selectedDate = todayStr;

        todayButton.setOnClickListener(v -> selectDate(todayButton, todayStr));
        tomorrowButton.setOnClickListener(v -> selectDate(tomorrowButton, tomorrowStr));
        thirdDateButton.setOnClickListener(v -> selectDate(thirdDateButton, thirdStr));
    }

    private void selectDate(Button selected, String dateValue) {
        selectedDate = dateValue;
        todayButton.setBackgroundResource(R.drawable.pill_inactive);
        todayButton.setTextColor(getColor(R.color.studdict_muted));
        tomorrowButton.setBackgroundResource(R.drawable.pill_inactive);
        tomorrowButton.setTextColor(getColor(R.color.studdict_muted));
        thirdDateButton.setBackgroundResource(R.drawable.pill_inactive);
        thirdDateButton.setTextColor(getColor(R.color.studdict_muted));

        selected.setBackgroundResource(R.drawable.pill_active);
        selected.setTextColor(getColor(R.color.studdict_surface));
    }

    private void selectPrivateMode() {
        publicMode = false;
        privateChoiceCard.setBackgroundResource(R.drawable.choice_selected);
        publicChoiceCard.setBackgroundResource(R.drawable.choice_unselected);
        subjectInput.setVisibility(View.GONE);
    }

    private void selectPublicMode() {
        publicMode = true;
        privateChoiceCard.setBackgroundResource(R.drawable.choice_unselected);
        publicChoiceCard.setBackgroundResource(R.drawable.choice_selected);
        subjectInput.setVisibility(View.VISIBLE);
    }

    private void setGroupSize(int size) {
        if (size >= 1 && size <= 10) {
            groupSize = size;
            groupSizeText.setText(String.valueOf(groupSize));
        }
    }

    private void showTimePicker() {
        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
            timeButton.setText(selectedTime + "  v");
        }, 10, 0, true).show();
    }

    private void showDurationPicker() {
        String[] durations = {"1 Hour", "2 Hours", "3 Hours", "4 Hours"};
        int[] mins = {60, 120, 180, 240};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Duration")
            .setItems(durations, (dialog, which) -> {
                selectedDuration = mins[which];
                durationButton.setText(durations[which] + "  v");
            })
            .show();
    }
}
