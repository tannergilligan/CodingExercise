package com.paragonintel.codingexercise.Events;

import com.google.gson.Gson;
import com.paragonintel.codingexercise.Location.GeoCoordinate;

import java.util.Date;

public class AdsbEvent {
    private static final Gson gson = new Gson();
    private String identifier;
    private Date timestamp;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double altitude = Double.NaN;
    private double speed = Double.NaN;
    private double heading = Double.NaN;

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public GeoCoordinate getGeoCoordinate() {
        return new GeoCoordinate(this.latitude, this.longitude);
    }

    public Double getAltitude() {
        return this.altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public Double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Double getHeading() {
        return this.heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public static AdsbEvent fromJson(String json) {
        return gson.fromJson(json, AdsbEvent.class);
    }
}
