package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.LoginRequest;
import com.studdict.mobile.model.Student;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenLogin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtGoToRegister = findViewById(R.id.txtGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiClient.getApi().loginStudent(new LoginRequest(email, password)).enqueue(new Callback<Student>() {
                @Override
                public void onResponse(Call<Student> call, Response<Student> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ScreenLogin.this, "Welcome " + response.body().getFirstName(), Toast.LENGTH_SHORT).show();
                        // Proceed to home/venues
                        Intent intent = new Intent(ScreenLogin.this, ScreenVenues.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ScreenLogin.this, "Login failed. Check credentials.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Student> call, Throwable t) {
                    Toast.makeText(ScreenLogin.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        txtGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(ScreenLogin.this, ScreenRegister.class);
            startActivity(intent);
        });
    }
}
