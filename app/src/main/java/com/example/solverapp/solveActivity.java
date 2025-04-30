package com.example.solverapp;

import android.content.Intent;
import android.database.Cursor;
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

import com.example.solverapp.DB.DBHelper;

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

        textView.setText(answer);
/////////
        int id = getIntent().getIntExtra("id", -1); //// Get the ID from the Intent
        if (id != -1) {
            DBHelper dbHelper = new DBHelper(this);
            Cursor cursor = dbHelper.getSingleData(id);

            if (cursor != null && cursor.moveToFirst()) {
                byte[] imageBytes = cursor.getBlob(1);
                Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                String solve = cursor.getString(2);

                imageView = findViewById(R.id.imageView1);
                textView = findViewById(R.id.answer);

                imageView.setImageBitmap(image);
                textView.setText(solve);

                cursor.close();
            }

        }
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