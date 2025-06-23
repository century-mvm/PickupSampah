package com.example.pickupsampah;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView txtUserdetails;
    private ImageView profileImage;
    private ImageButton btnNotification, btnChat;
    private Button buttonLogout, buttonRequest;

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_CODE = 100;

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
        profileImage = findViewById(R.id.profile_image);
        btnNotification = findViewById(R.id.btn_notification);

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

        // Notification button action
        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show());

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
        LatLng widyatama = new LatLng(-6.901200, 107.618000);
        mMap.addMarker(new MarkerOptions()
                .position(widyatama)
                .title("Universitas Widyatama"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(widyatama, 16));

        // Tampilkan lokasi user jika izin diberikan
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            // Dapatkan lokasi terakhir dan pindahkan kamera
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
                    mMap.addMarker(new MarkerOptions().position(userLoc).title("Lokasi Anda"));
                }
            });
        } else {
            // Minta izin jika belum diberikan
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

        View mapView = findViewById(R.id.map_fragment);
        mapView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FullscreenMapActivity.class);
            startActivity(intent);
        });
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
