package com.paragonintel.codingexercise.Airports;

import com.paragonintel.codingexercise.Location.GeoCoordinate;

public class Airport {
    private String identifier;
    private double longitude;
    private double latitude;
    private int elevation;

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getElevation() {
        return this.elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public GeoCoordinate getGeoCoordinate() {
        return new GeoCoordinate(this.latitude, this.longitude);
    }
}
