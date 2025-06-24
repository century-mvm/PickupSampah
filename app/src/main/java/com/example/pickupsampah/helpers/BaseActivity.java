package com.example.pickupsampah.helpers;

import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        checkInternet();
    }

    private void checkInternet() {
        if (!NetworkUtil.isConnected(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setMessage("This app requires an internet connection. Please connect to Wi-Fi or mobile data.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
