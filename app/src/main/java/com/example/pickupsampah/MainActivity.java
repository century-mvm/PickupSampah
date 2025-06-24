package com.example.pickupsampah;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView txtUserdetails;
    private ImageView profileImage;
    private ImageButton buttonLogout;
    private Button buttonRequest;

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private final DatabaseReference pickupRef = FirebaseDatabase.getInstance(
            "https://pickupsampah-k4-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("pickup_orders");


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        View mapOverlay = findViewById(R.id.map_overlay);
        mapOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FullscreenMapActivity.class);
            startActivity(intent);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase auth & user check
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), LandingActivity.class));
            finish();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
        // UI Binding
        txtUserdetails = findViewById(R.id.userDetails);
        buttonLogout = findViewById(R.id.LgtBtn);
        buttonRequest = findViewById(R.id.btn_request);
        //profileImage = findViewById(R.id.profile_image);

        txtUserdetails.setText(user.getEmail());

        // Logout handler
        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LandingActivity.class));
            finish();
        });

        buttonRequest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PickupRequestActivity.class);
            startActivity(intent);
        });


        // Map init
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Aktifkan zoom, compass, dll
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Lokasi default: Widyatama
        LatLng widyatama = new LatLng(-6.8978533091074095, 107.64539057116501);
        mMap.addMarker(new MarkerOptions()
                .position(widyatama)
                .title("Universitas Widyatama"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(widyatama, 16));

        // Tampilkan lokasi user jika izin diberikan
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            // Dapatkan lokasi terakhir dan pindahkan maps
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

        View mapView = findViewById(R.id.map_fragment);
        mapView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FullscreenMapActivity.class);
            startActivity(intent);
        });

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
                Toast.makeText(MainActivity.this, "Gagal memuat data pickup.", Toast.LENGTH_SHORT).show();
            }
        });

        // Lihat Detail
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof PickupOrder) {
                PickupOrder order = (PickupOrder) tag;
                showDetailDialog(order);
                return true;
            }
            return false;
        });
    }

    private void showDetailDialog(PickupOrder order) {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        layout.addView(timeView);

        builder.setView(layout);

        builder.setPositiveButton("Tutup", null);
        builder.setNegativeButton("Hapus", (dialog, which) -> deletePickupOrder(order));

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
                                        Toast.makeText(MainActivity.this, "Pickup berhasil dihapus", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(MainActivity.this, "Gagal menghapus pickup", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Gagal mengakses database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap base64ToBitmap(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Izin lokasi wajib diberikan untuk menggunakan peta.", Toast.LENGTH_LONG).show();
            }
        }
    }
}