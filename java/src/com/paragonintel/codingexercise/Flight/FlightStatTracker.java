package com.paragonintel.codingexercise.Flight;

import com.paragonintel.codingexercise.Events.AdsbEvent;
import com.paragonintel.codingexercise.Location.GeoCoordinate;

import java.util.*;

/**
 * This class has 2 main purposes:
 *  1) Keep track of the earliest events with valid speed/altitude/coordinate
 *  2) Keep track of the latest events with valid speed/altitude/coordinate
 *
 * Tracker gets reset when a plane takes off, and is used to generate a Flight
 * model once the plane has landed.
 */
public class FlightStatTracker {

    private AdsbEvent earliestSpeedEvent;
    private AdsbEvent earliestAltitudeEvent;
    private AdsbEvent earliestCoordinateEvent;

    private AdsbEvent latestSpeedEvent;
    private AdsbEvent latestAltitudeEvent;
    private AdsbEvent latestCoordinateEvent;

    public FlightStatTracker() {}

    // Resets the tracker, so the most recent event is now the only event
    // This is invoked to effectively 'start' the flight.
    public void reset() {
        earliestSpeedEvent = latestSpeedEvent;
        earliestCoordinateEvent = latestCoordinateEvent;
        earliestAltitudeEvent = latestAltitudeEvent;
    }

    public void processEvent(AdsbEvent event) {
        if (event == null) return;

        // If the event has a speed, update the earliest/latest speed
        if (event.getSpeed() != null && !event.getSpeed().isNaN()) {
            latestSpeedEvent = event;
            if (earliestSpeedEvent == null) earliestSpeedEvent = event;
        }

        // If the event has an altitude, update the earliest/latest altitude
        if (event.getAltitude() != null && !event.getAltitude().isNaN()) {
            latestAltitudeEvent = event;
            if (earliestAltitudeEvent == null) earliestAltitudeEvent = event;
        }

        // If the event has a lat/long, update the earliest/latest lat/long
        if (event.getLatitude() != null && !event.getLatitude().isNaN() &&
            event.getLongitude() != null && !event.getLongitude().isNaN()) {
            latestCoordinateEvent = event;
            if (earliestCoordinateEvent == null) {
                earliestCoordinateEvent = latestCoordinateEvent;
            }
        }
    }

    // Returns the timestamp of the earliest event with a valid coordinate
    public Date getEarliestCoordinateTime() {
        if (earliestCoordinateEvent == null) return null;
        return earliestCoordinateEvent.getTimestamp();
    }

    public Double getEarliestSpeed() {
        if (earliestSpeedEvent == null) return null;
        return earliestSpeedEvent.getSpeed();
    }

    public Double getEarliestAltitude() {
        if (earliestAltitudeEvent == null) return null;
        return earliestAltitudeEvent.getAltitude();
    }

    public GeoCoordinate getEarliestCoordinate() {
        if (earliestCoordinateEvent == null) return null;
        return new GeoCoordinate(
                earliestCoordinateEvent.getLatitude(),
                earliestCoordinateEvent.getLongitude()
        );
    }

    // Of the 3 most recent events, returns the maximum timestamp.
    public Date getLatestTime() {
        List<Date> timestamps = new ArrayList<>();
        if (latestSpeedEvent != null) timestamps.add(latestSpeedEvent.getTimestamp());
        if (latestAltitudeEvent != null) timestamps.add(latestAltitudeEvent.getTimestamp());
        if (latestCoordinateEvent != null) timestamps.add(latestCoordinateEvent.getTimestamp());

        if (timestamps.isEmpty()) return null;
        Collections.sort(timestamps);
        return timestamps.get(timestamps.size() - 1);
    }

    public Double getLatestSpeed() {
        if (latestSpeedEvent == null) return null;
        return latestSpeedEvent.getSpeed();
    }

    public Double getLatestAltitude() {
        if (latestAltitudeEvent == null) return null;
        return latestAltitudeEvent.getAltitude();
    }

    public GeoCoordinate getLatestCoordinate() {
        return new GeoCoordinate(
            latestCoordinateEvent.getLatitude(),
            latestCoordinateEvent.getLongitude()
        );
    }
}
