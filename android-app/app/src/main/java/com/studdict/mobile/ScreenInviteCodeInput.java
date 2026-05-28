package com.studdict.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.InviteJoinResponse;
import com.studdict.mobile.model.JoinInviteCodeRequest;
import com.studdict.mobile.model.ValidateInviteCodeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenInviteCodeInput extends Activity {

    private EditText codeInput;
    private TextView statusText;

    private String guestId = "S2"; // Mock guest student for UC3 testing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        title.setText("Enter Invite Code");
        title.setTextSize(30);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.START);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Type the invite code shared by the host.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, 12, 0, 24);
        root.addView(subtitle);

        codeInput = new EditText(this);
        codeInput.setHint("e.g. 123456");
        codeInput.setTextSize(20);
        codeInput.setSingleLine(true);
        codeInput.setPadding(24, 16, 24, 16);
        root.addView(codeInput);

        Button confirmButton = new Button(this);
        confirmButton.setText("Confirm Code");
        confirmButton.setAllCaps(false);
        confirmButton.setOnClickListener(v -> submitCode());
        root.addView(confirmButton);

        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(18);
        statusText.setTextColor(Color.DKGRAY);
        statusText.setPadding(0, 24, 0, 0);
        root.addView(statusText);

        setContentView(root);
    }

    private void submitCode() {
        String code = codeInput.getText().toString().trim();

        if (code.isEmpty()) {
            showMessage("Please enter an invite code.");
            return;
        }

        validateCode(code);
    }

    private void validateCode(String code) {
        ValidateInviteCodeRequest request = new ValidateInviteCodeRequest(code);

        ApiClient.getApi().validateInviteCode(request).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                    joinReservation(code);
                } else {
                    showMessage("Invalid or expired invite code. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                showMessage("Network Error: " + t.getMessage());
            }
        });
    }

    private void joinReservation(String code) {
        JoinInviteCodeRequest request = new JoinInviteCodeRequest(code, guestId);

        ApiClient.getApi().joinReservationWithInviteCodeResult(request).enqueue(new Callback<InviteJoinResponse>() {
            @Override
            public void onResponse(Call<InviteJoinResponse> call, Response<InviteJoinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InviteJoinResponse result = response.body();
                    showMessage(result.getMessage());
                } else {
                    showMessage("Could not join reservation. HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<InviteJoinResponse> call, Throwable t) {
                showMessage("Network Error: " + t.getMessage());
            }
        });
    }

    private void showMessage(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}