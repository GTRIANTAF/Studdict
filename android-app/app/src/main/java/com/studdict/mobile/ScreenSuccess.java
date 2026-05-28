package com.studdict.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ScreenSuccess extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Simple success message could be a layout, but let's just show a button to go home
        setContentView(android.R.layout.select_dialog_item); // Using a default layout for simplicity
        
        // Actually it's better to have a proper layout, but for now let's just finish or go back
        Button btn = new Button(this);
        btn.setText("Success! Go Home");
        setContentView(btn);
        
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScreenVenues.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}