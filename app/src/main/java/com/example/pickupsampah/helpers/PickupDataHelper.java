package com.example.pickupsampah.helpers;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.pickupsampah.PickupOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class PickupDataHelper {
    public static void deletePickupOrder(Context context, PickupOrder order, DatabaseReference pickupRef) {
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
                                        Toast.makeText(context, "Pickup berhasil dihapus", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Gagal menghapus pickup", Toast.LENGTH_SHORT).show());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Gagal mengakses database", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
