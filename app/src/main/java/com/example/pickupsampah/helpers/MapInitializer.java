package com.example.pickupsampah.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pickupsampah.PickupOrder;
import com.example.pickupsampah.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MapInitializer {

    public static final int LOCATION_PERMISSION_CODE = 100;

    public static void setupMap(
            @NonNull Activity activity,
            @NonNull GoogleMap map,
            @NonNull DatabaseReference pickupRef
    ) {

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);

        // Lokasi default: Widyatama
        LatLng widyatama = new LatLng(-6.8978533091074095, 107.64539057116501);
        map.addMarker(new MarkerOptions().position(widyatama).title("Universitas Widyatama"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(widyatama, 16));

        // Izin lokasi
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            // Lokasi terakhir pengguna
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
            fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
                if (location != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 17));
                }
            });
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }

        // Marker Firebase
        pickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                map.clear();
                map.addMarker(new MarkerOptions().position(widyatama).title("Universitas Widyatama"));


                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    PickupOrder order = orderSnap.getValue(PickupOrder.class);
                    if (order != null) {
                        LatLng lokasi = new LatLng(order.getLatitude(), order.getLongitude());
                        BitmapDescriptor icon = getBitmapFromVector(activity, R.drawable.ic_trash_bin); // Ubah ukurannya di sini

                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(lokasi)
                                .title("Klik untuk lihat deskripsi")
                                .icon(icon));


                        if (marker != null) {
                            marker.setTag(order);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    Toast.makeText(activity, "Gagal memuat data pickup.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        map.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof PickupOrder) {
                PickupOrder order = (PickupOrder) tag;
                PickupDialogHelper.showDetailDialog(activity, order, pickupRef);
                return true;
            }
            return false;
        });
    }
    private static BitmapDescriptor getBitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            throw new IllegalArgumentException("Resource not found: " + vectorResId);
        }

        // Buat ukuran sesuai yang diminta (lebar x tinggi)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
