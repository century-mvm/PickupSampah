package com.example.pickupsampah.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.pickupsampah.PickupOrder;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PickupDialogHelper {
    public static void showDetailDialog(Context context, PickupOrder order, DatabaseReference pickupRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Detail Pickup");

        // Gambar
        ImageView imageView = new ImageView(context);
        Bitmap imageBitmap = ImageUtil.base64ToBitmap(order.getImageBase64());
        imageView.setImageBitmap(imageBitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight(600);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Deskripsi
        TextView descView = new TextView(context);
        descView.setText("Deskripsi: " + order.getDescription());
        descView.setPadding(0, 20, 0, 0);

        // Waktu Submit
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault());
        TextView timeView = new TextView(context);
        timeView.setText("Waktu Submit: " + sdf.format(new Date(order.getTimestamp())));
        timeView.setPadding(0, 10, 0, 0);

        // Layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);
        layout.addView(imageView);
        layout.addView(descView);
        layout.addView(timeView);

        builder.setView(layout);

        builder.setPositiveButton("Tutup", null);
        builder.setNegativeButton("Hapus Pickup", (dialog, which) ->
                PickupDataHelper.deletePickupOrder(context, order, pickupRef));

        builder.show();
    }
}
