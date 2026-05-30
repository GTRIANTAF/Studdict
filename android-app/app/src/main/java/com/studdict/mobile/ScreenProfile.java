package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ScreenProfile extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SessionManager session = new SessionManager(this);

        String firstName = session.getFirstName();
        String lastName = session.getLastName();
        String fullName = ((firstName != null ? firstName : "") + " "
                + (lastName != null ? lastName : "")).trim();

        ((TextView) findViewById(R.id.profileName)).setText(fullName.isEmpty() ? "—" : fullName);
        setField(R.id.profileEmailSub, session.getEmail());
        setField(R.id.profileFirstName, firstName);
        setField(R.id.profileLastName, lastName);
        setField(R.id.profileEmail, session.getEmail());
        setField(R.id.profileUniversity, session.getUniversity());
        setField(R.id.profileDepartment, session.getDepartment());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setField(int viewId, String value) {
        TextView tv = findViewById(viewId);
        tv.setText(value != null && !value.isEmpty() ? value : "—");
    }

    private void logout() {
        // Clear any persisted check-in session and the stored profile so the
        // next login starts fresh.
        SessionManager session = new SessionManager(this);
        session.clearCheckIn();
        session.clearProfile();

        // Return to the login screen and wipe the back stack so the user
        // can't navigate back into the logged-in flow.
        Intent intent = new Intent(this, ScreenLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
