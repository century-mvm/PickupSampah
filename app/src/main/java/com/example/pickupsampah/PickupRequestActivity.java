package com.example.pickupsampah;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class PickupRequestActivity extends AppCompatActivity {

    private EditText inputDesc;
    private ImageView previewImage;
    private Button btnSubmit, btnCamera;

    private Bitmap capturedImage;
    private double latitude = 0.0, longitude = 0.0;

    private FusedLocationProviderClient locationClient;
    private static final int CAMERA_REQUEST = 101;

    DatabaseReference pickupRef = FirebaseDatabase.getInstance(
            "https://pickupsampah-k4-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("pickup_orders");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_request);

        inputDesc = findViewById(R.id.edit_description);
        previewImage = findViewById(R.id.image_preview);
        btnCamera = findViewById(R.id.btn_camera);
        btnSubmit = findViewById(R.id.btn_submit);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ambil lokasi saat ini
        getCurrentLocation();

        // Ambil foto
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        });

        // Submit request
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
        byte[] imageBytes = out.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Hasil dari kamera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            previewImage.setImageBitmap(capturedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
