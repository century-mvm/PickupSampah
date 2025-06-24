package com.example.pickupsampah;

public class PickupOrder {
    private double latitude;
    private double longitude;
    private String description;
    private String imageBase64;
    private long timestamp;

    public PickupOrder() {
        this.timestamp = System.currentTimeMillis();
    }

    public PickupOrder(double latitude, double longitude, String description, String imageBase64) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.timestamp = System.currentTimeMillis();
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getImageBase64() { return imageBase64; }
    public long getTimestamp() { return timestamp; }
}
