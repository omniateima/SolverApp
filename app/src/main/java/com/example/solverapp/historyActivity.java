package com.example.solverapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.solverapp.DB.DBHelper;

import java.util.ArrayList;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DBHelper dbHelper= new DBHelper(this);
        Cursor cursor = dbHelper.getAllDataCursor();
        ArrayList<DataClass> dataList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                System.out.println("Column1: " + cursor.getString(0));
                int id = cursor.getInt(0);
                byte[] imageBytes = cursor.getBlob(1);
                String answer = cursor.getString(2);
                if (imageBytes != null) {
                    Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    dataList.add(new DataClass(id, image,answer));
                } else {
                    System.out.println("Image data is null for ID: " + id);
                }
            }
            cursor.close();
        }else{
            throw new IllegalStateException("Cursor is null. Check your database query.");

         }

        ChatAdapter chatAdapter = new ChatAdapter(this, dataList);
        recyclerView.setAdapter(chatAdapter);
        //chatAdapter.getItemId(R.id.solution);
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