package com.example.pickupsampah;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.*;

public class FullscreenMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_CODE = 200;

    private final DatabaseReference pickupRef = FirebaseDatabase.getInstance(
            "https://pickupsampah-k4-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("pickup_orders");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.full_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Marker tetap: Widyatama
        LatLng widyatama = new LatLng(-6.901200, 107.618000);
        mMap.addMarker(new MarkerOptions().position(widyatama).title("Universitas Widyatama"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(widyatama, 16));

        // Lokasi pengguna
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("Lokasi Anda"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
        }

        // Ambil data dari Firebase
        pickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();

                // Tambahkan ulang marker statis
                mMap.addMarker(new MarkerOptions().position(widyatama).title("Universitas Widyatama"));

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    PickupOrder order = orderSnap.getValue(PickupOrder.class);
                    if (order != null) {
                        LatLng lokasi = new LatLng(order.getLatitude(), order.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(lokasi)
                                .title("Klik untuk lihat deskripsi"));
                        if (marker != null) {
                            marker.setTag(order);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FullscreenMapActivity.this, "Gagal memuat data pickup.", Toast.LENGTH_SHORT).show();
            }
        });

        // Klik marker untuk lihat detail
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof PickupOrder) {
                PickupOrder order = (PickupOrder) tag;
                showDetailDialog(order);
                return true; // Cegah info bawaan tampil
            }
            return false;
        });
    }

    private void showDetailDialog(PickupOrder order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail Pickup");

        // Gambar
        ImageView imageView = new ImageView(this);
        Bitmap imageBitmap = base64ToBitmap(order.getImageBase64());
        imageView.setImageBitmap(imageBitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(600);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Deskripsi
        TextView descView = new TextView(this);
        descView.setText("Deskripsi: " + order.getDescription());
        descView.setPadding(0, 20, 0, 0);

        // Format timestamp
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm", java.util.Locale.getDefault());
        String formattedTime = sdf.format(new java.util.Date(order.getTimestamp()));

        // Waktu Submit
        TextView timeView = new TextView(this);
        timeView.setText("Waktu Submit: " + formattedTime);
        timeView.setPadding(0, 10, 0, 0);

        // Layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);
        layout.addView(imageView);
        layout.addView(descView);
        layout.addView(timeView); // <== Tambahkan di sini

        builder.setView(layout);

        builder.setPositiveButton("Tutup", null);
        builder.setNegativeButton("Hapus Pickup", (dialog, which) -> deletePickupOrder(order));

        builder.show();
    }


    private void deletePickupOrder(PickupOrder order) {
        pickupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    PickupOrder current = child.getValue(PickupOrder.class);
                    if (current != null &&
                            current.getLatitude() == order.getLatitude() &&
                            current.getLongitude() == order.getLongitude() &&
                            current.getDescription().equals(order.getDescription())) {

                        child.getRef().removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(FullscreenMapActivity.this, "Pickup berhasil dihapus", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(FullscreenMapActivity.this, "Gagal menghapus pickup", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FullscreenMapActivity.this, "Gagal mengakses database", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private Bitmap base64ToBitmap(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
