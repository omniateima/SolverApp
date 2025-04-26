package com.example.solverapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.solverapp.DB.DBHelper;
import com.example.solverapp.dto.request.ModelRequest;
import com.example.solverapp.dto.response.ModelResponse;
import com.example.solverapp.service.ModelService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final MutableLiveData<String> modelResponseLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> modelErrorLiveData = new MutableLiveData<>();


    Button historyBtn;
    ImageButton uploadBtn;
    Retrofit retrofit;
    ModelService modelService;
    ////////////
    DBHelper dbHelper = new DBHelper(this);
    //////////////
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;


    private static final String BASE_URL = "https://integrate.api.nvidia.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        init();

        historyBtn.setOnClickListener(v -> goToHistoryActivity());
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request permissions if needed
                if (checkPermissions()) {
                    openGallery();
                } else {
                    requestPermissions();
                }
            }
        });


    }

    private void init() {
        historyBtn = findViewById(R.id.history);
        uploadBtn = findViewById(R.id.uploadButton);
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        modelService = retrofit.create(ModelService.class);


        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean isGranted : permissions.values()) {
                        allGranted = allGranted && isGranted;
                    }

                    if (allGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Permission denied. Cannot access gallery.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            // Get the bitmap from the selected image
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImageUri);


                            // Display the selected image
//                            imageView.setImageBitmap(bitmap);

                            // Convert the bitmap to Base64 and display it
                            String base64String = bitmapToBase64(bitmap);
//                            Log.i("Image", base64String);
                            sendToModelAPI(base64String);
                            modelResponseLiveData.observe(this, r -> {
                                Intent resultIntent = new Intent(MainActivity.this, solveActivity.class);
                                resultIntent.putExtra("image", base64String);
                                resultIntent.putExtra("answer", r);
                                startActivity(resultIntent);
                                //////
                                boolean isInserted = dbHelper.insertData(bitmap, r);
                                if (!isInserted)  {
                                    Toast.makeText(this, "Failed to insert data.", Toast.LENGTH_SHORT).show();
                                }
                                //////
                            });

                            modelErrorLiveData.observe(this, error -> {
                                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            });
//                            Intent resultIntent = new Intent(MainActivity.this, solveActivity.class);
//                            resultIntent.putExtra("answer", response);
//                            startActivity(resultIntent);
//                            textBase64.setText(base64String);

                            // Log a portion of Base64 string (might be too large to log completely)
//                            Log.d(TAG, "Base64 (first 100 chars): " +
//                                    base64String.substring(0, Math.min(base64String.length(), 100)));

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void goToHistoryActivity() {
        Intent intent = new Intent(this, historyActivity.class);
        startActivity(intent);
    }

    private void sendToModelAPI(String image) {
        modelService.send(new ModelRequest(image)).enqueue(new Callback<ModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<ModelResponse> call, @NonNull Response<ModelResponse> response) {
                if (response.body() != null) {
                    String modelResponseAsString = response.body().getMessage();
                    Log.i("Model", modelResponseAsString);
                    modelResponseLiveData.postValue(modelResponseAsString);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ModelResponse> call, @NonNull Throwable t) {
                Log.i("TAG", "onFailure: " + t.getMessage());
                modelErrorLiveData.postValue(t.getMessage());
            }
        });
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+)
            return ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // For Android 12 and below
            return ContextCompat.checkSelfPermission(this,
                    READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(READ_MEDIA_IMAGES);
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(READ_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Compress the image (adjust quality as needed)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}