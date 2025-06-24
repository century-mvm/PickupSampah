package com.example.pickupsampah;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.pickupsampah.helpers.BaseActivity;
import com.example.pickupsampah.helpers.MapInitializer;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FullscreenMapActivity extends BaseActivity implements OnMapReadyCallback {

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
        MapInitializer.setupMap(this, googleMap, pickupRef);
    }
}
