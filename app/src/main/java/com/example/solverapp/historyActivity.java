package com.example.solverapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class historyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView left_arrow_main = findViewById(R.id.left_arrow_main);
        Button solution = findViewById(R.id.solution);
        left_arrow_main.setOnClickListener(v->{
            goToMainActivity();
        });
        solution.setOnClickListener(v->{
            goToSolveActivity();
        });
    }
private void goToMainActivity(){
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
}
private void goToSolveActivity(){
    Intent intent = new Intent(this, solveActivity.class);
    startActivity(intent);
}
}