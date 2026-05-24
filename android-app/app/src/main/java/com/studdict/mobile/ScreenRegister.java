package com.studdict.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.studdict.mobile.api.ApiClient;
import com.studdict.mobile.model.RegisterRequest;
import com.studdict.mobile.model.Student;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScreenRegister extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText editFirstName = findViewById(R.id.editFirstName);
        EditText editLastName = findViewById(R.id.editLastName);
        EditText editEmail = findViewById(R.id.editRegEmail);
        EditText editPassword = findViewById(R.id.editRegPassword);
        EditText editUniversity = findViewById(R.id.editUniversity);
        EditText editDepartment = findViewById(R.id.editDepartment);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            RegisterRequest request = new RegisterRequest(
                    editFirstName.getText().toString(),
                    editLastName.getText().toString(),
                    editEmail.getText().toString(),
                    editPassword.getText().toString(),
                    editUniversity.getText().toString(),
                    editDepartment.getText().toString()
            );

            ApiClient.getApi().registerStudent(request).enqueue(new Callback<Student>() {
                @Override
                public void onResponse(Call<Student> call, Response<Student> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ScreenRegister.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish(); // return to login
                    } else {
                        // Usually backend returns 400 with "Email already exists"
                        Toast.makeText(ScreenRegister.this, "Error: Email might already exist.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Student> call, Throwable t) {
                    Toast.makeText(ScreenRegister.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
