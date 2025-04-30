package com.example.solverapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.CAMERA;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.solverapp.DB.DBHelper;
import com.example.solverapp.dto.request.ModelRequest;
import com.example.solverapp.dto.response.ModelResponse;
import com.example.solverapp.service.ModelService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    ImageButton cameraBtn;
    Retrofit retrofit;
    ModelService modelService;
    /// /////////
    DBHelper dbHelper = new DBHelper(this);
    /// ///////////
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    private Uri photoUri;
    private String currentPhotoPath;

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
                if (checkGalleryPermissions()) {
                    openGallery();
                } else {
                    requestGalleryPermissions();
                }
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request permissions if needed
                if (checkCameraPermissions()) {
                    openCamera();
                } else {
                    requestCameraPermissions();
                }
            }
        });
    }

    private void init() {
        historyBtn = findViewById(R.id.history);
        uploadBtn = findViewById(R.id.uploadButton);
        cameraBtn = findViewById(R.id.cameraButton);
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
                        if (permissions.containsKey(CAMERA)) {
                            openCamera();
                        } else {
                            openGallery();
                        }
                    } else {
                        Toast.makeText(this, "Permission denied.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        processImageUri(selectedImageUri);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        processCapturedImage();
                    }
                });
    }

    private void processImageUri(Uri imageUri) {
        try {
            // Get the bitmap from the selected image
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);

            // Convert the bitmap to Base64 and display it
            String base64String = bitmapToBase64(bitmap);
            processImageData(bitmap, base64String);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void processCapturedImage() {
        if (photoUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                String base64String = bitmapToBase64(bitmap);
                processImageData(bitmap, base64String);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load captured image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processImageData(Bitmap bitmap, String base64String) {
        sendToModelAPI(base64String);

        modelResponseLiveData.observe(this, r -> {
            Intent resultIntent = new Intent(MainActivity.this, solveActivity.class);
            resultIntent.putExtra("image", base64String);
            resultIntent.putExtra("answer", r);
            startActivity(resultIntent);
            //////
            boolean isInserted = dbHelper.insertData(bitmap, r);
            if (!isInserted) {
                Toast.makeText(this, "Failed to insert data.", Toast.LENGTH_SHORT).show();
            }
            //////
        });

        modelErrorLiveData.observe(this, error -> {
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
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

    private boolean checkGalleryPermissions() {
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

    private boolean checkCameraPermissions() {
        return ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGalleryPermissions() {
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

    private void requestCameraPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(CAMERA);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            openCamera();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.solverapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

//    private String bitmapToBase64(Bitmap bitmap) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        // Compress the image (adjust quality as needed)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
//
//        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(byteArray, Base64.DEFAULT);
//    }
private String bitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int quality = 70; // Start with 70% quality
    float scale = 1.0f; // Start with original size
    Bitmap resizedBitmap = bitmap;
    String base64String;
    boolean createdNewBitmap = false;

    // First attempt: try with original size but different compression quality
    do {
        byteArrayOutputStream.reset(); // Clear the stream
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // If still too large, reduce quality
        if (base64String.length() > 180000 && quality > 10) {
            quality -= 10;
        }
        // If quality is already low, start scaling down the image
        else if (base64String.length() > 180000) {
            // Reduce scale by 10% each time
            scale *= 0.9f;
            int newWidth = (int) (bitmap.getWidth() * scale);
            int newHeight = (int) (bitmap.getHeight() * scale);

            // Don't let dimensions get too small
            if (newWidth < 200 || newHeight < 200) {
                // If we're at the minimum size, use the lowest quality and break
                quality = 10;
                byteArrayOutputStream.reset();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                byte[] finalByteArray = byteArrayOutputStream.toByteArray();
                base64String = Base64.encodeToString(finalByteArray, Base64.DEFAULT);
                break;
            }

            // Create a new, smaller bitmap
            if (createdNewBitmap) {
                resizedBitmap.recycle(); // Recycle old resized bitmap to free memory
            }
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            createdNewBitmap = true;
            quality = 70; // Reset quality for the new size
        }
    } while (base64String.length() > 180000);

    // Log the final dimensions and quality for debugging
    Log.d("ImageProcessing", "Final image: " + resizedBitmap.getWidth() + "x" +
            resizedBitmap.getHeight() + " quality: " + quality +
            " base64 length: " + base64String.length());

    // IMPORTANT: Make the input bitmap match the resized one
    if (createdNewBitmap && bitmap != resizedBitmap) {
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        // Create a Canvas to draw the resized bitmap onto the original one
        Canvas canvas = new Canvas(bitmap);
        // Scale the canvas to match the dimensions
        float scaleX = (float) bitmap.getWidth() / resizedBitmap.getWidth();
        float scaleY = (float) bitmap.getHeight() / resizedBitmap.getHeight();
        canvas.scale(scaleX, scaleY);
        // Draw the resized bitmap onto the original
        canvas.drawBitmap(resizedBitmap, 0, 0, null);

        // Clean up the resized bitmap
        resizedBitmap.recycle();
    }

    return base64String;
}
}