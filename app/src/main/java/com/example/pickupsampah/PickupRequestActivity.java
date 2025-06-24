package com.example.pickupsampah;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.pickupsampah.helpers.BaseActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class PickupRequestActivity extends BaseActivity {

    private EditText inputDesc;
    private ImageView previewImage;
    private Bitmap capturedImage;
    private double latitude = 0.0, longitude = 0.0;

    private FusedLocationProviderClient locationClient;
    private DatabaseReference pickupRef;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data");
                        previewImage.setImageBitmap(capturedImage);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_request);

        inputDesc = findViewById(R.id.edit_description);
        previewImage = findViewById(R.id.image_preview);
        Button btnCamera = findViewById(R.id.btn_camera);
        Button btnSubmit = findViewById(R.id.btn_submit);

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        pickupRef = FirebaseDatabase.getInstance(
                "https://pickupsampah-k4-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("pickup_orders");

        getCurrentLocation();

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> {
            if (capturedImage == null || latitude == 0.0 || longitude == 0.0) {
                Toast.makeText(this, "Isi semua data & ambil gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            String desc = inputDesc.getText().toString().trim();
            String imageBase64 = bitmapToBase64(capturedImage);

            PickupOrder order = new PickupOrder(latitude, longitude, desc, imageBase64);
            String id = pickupRef.push().getKey();
            if (id != null) {
                pickupRef.child(id).setValue(order);
                Toast.makeText(this, "Berhasil dikirim", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    200
            );
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        byte[] imageBytes = out.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Optional: Handle permission result if you want full control
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // ✅ Add this line

        if (requestCode == 200 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
        }
    }
}
