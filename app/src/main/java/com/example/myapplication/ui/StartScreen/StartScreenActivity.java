package com.example.myapplication.ui.StartScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_startscreen); // Replace with your actual layout resource

        // Initialize Views
        //TextView textView = findViewById(R.id.text_StartScreen);  // Ensure that these IDs exist in your layout XML
        Button button = findViewById(R.id.button);

        // Set text for the TextView (replace with your own logic or ViewModel if needed)
        //textView.setText("Welcome to the Start Screen!");

        // Set text for the button (you can change the button label dynamically as well)
        //button.setText("Go to Welcome Activity");

        // Set the onClickListener for the button
        button.setOnClickListener(view -> {
            // Start the WelcomeActivity when the button is clicked
            Intent intent = new Intent(StartScreenActivity.this, WelcomeActivity.class);
            startActivity(intent);
        });
    }
}
