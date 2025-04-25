package com.example.solverapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class solveActivity extends AppCompatActivity {
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_solve);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        ImageView history = findViewById(R.id.history_icon);
        ImageView left_arrow = findViewById(R.id.left_arrow);

        left_arrow.setOnClickListener(v -> {
            goToMainActivity();
        });
        history.setOnClickListener(v -> {
            goToHistoryActivity();
        });

//        String base64Image = getIntent().getStringExtra("base64_image");
        String answer = getIntent().getStringExtra("answer");
        String base64Image = getIntent().getStringExtra("image");

        if (base64Image != null && !base64Image.isEmpty()) {
            // Convert base64 back to bitmap and display
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Log.i("Answer", answer);
        textView.setText(answer);

    }

    private void init() {
        imageView = findViewById(R.id.imageView1);
        textView = findViewById(R.id.answer);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToHistoryActivity() {
        Intent intent = new Intent(this, historyActivity.class);
        startActivity(intent);
    }
}