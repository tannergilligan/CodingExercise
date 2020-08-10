package com.paragonintel.codingexercise.Flight;

import com.paragonintel.codingexercise.Airports.Airport;
import com.paragonintel.codingexercise.Airports.AirportCollection;
import com.paragonintel.codingexercise.Events.AdsbEvent;
import com.paragonintel.codingexercise.Location.GeoCoordinate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This purpose of this class is to maintain the state of the plane, monitor its actions,
 * and decide when the plane has taken off or landed. As the plane takes off and lands,
 * new Flight objects are created to represent each trip.
 */

public class PlaneTracker {

    // Thresholds for determining if a plane is airborne or not
    private static final Double ALTITUDE_DIFFERENCE_THRESHOLD = 500D;
    private static final Double SPEED_THRESHOLD = 150D;
    private static final Double AIRPORT_DISTANCE_THRESHOLD = 3D;

    // If we have recently changed from (airborne -> landed) or (landed -> airborne),
    // prevent us from transitioning back for at least this long. This prevents cases
    // where planes are traveling on the threshold between airborne and not.
    private static final Long MIN_TRANSITION_DELAY = 10 * 60 * 1000L;

    // Defines the window-size for the distance-to-airport moving average
    private static final Long MOVING_AVERAGE_TIME_WINDOW = 5 * 60 * 1000L;  // 5 minutes

    private final String planeId;
    private final List<Flight> flights;
    private final AirportCollection airports;
    private final FlightStatTracker flightStatsTracker;
    private final MovingAverageCalculator averageDistanceToAirport;

    private Boolean airborne;
    private Date lastTransitionTime;
    private Airport mostRecentlyVisited;
    private Airport latestClosestAirport;

    public PlaneTracker(String planeId, AirportCollection airports) {
        this.flightStatsTracker = new FlightStatTracker();
        this.planeId = planeId;
        this.airports = airports;
        this.flights = new ArrayList<>();
        this.averageDistanceToAirport = new MovingAverageCalculator(MOVING_AVERAGE_TIME_WINDOW);

        this.airborne = null;  // Null to start, since we don't know if we're airborne or not
        this.lastTransitionTime = new Date(0L);
    }

    /**
     * Returns the computed flights for this PlaneTracker. If there are any flights
     * in progress (e.g. still airborne), create a departure-only Flight
     */
    public List<Flight> getFlights() {
        List<Flight> flightsCopy = new ArrayList(flights);
        if (airborne) {
            Flight inProgressFlight = generateFlight();
            if (inProgressFlight != null) flightsCopy.add(inProgressFlight);
        }
        return flightsCopy;
    }

    /**
     * Given an event, extracts the information, and updates all relevant metadata.
     * It is assumed that events are based in time-series order.
     * @param event The next AdsbEvent for this plane.
     */
    public void processEvent(AdsbEvent event) {

        // Sanity check to make sure we were passed an event for the right plane
        if (!event.getIdentifier().equals(planeId)) {
            String msg = "Passed event for ID " + event.getIdentifier() + " to PlaneTracker for ID " + planeId;
            throw new RuntimeException(msg);
        }

        // Process the event
        flightStatsTracker.processEvent(event);

        // Figure out what fields the event has available
        Boolean hasLatitude = (event.getLatitude() != null) && !event.getLatitude().isNaN();
        Boolean hasLongitude = (event.getLongitude() != null) && !event.getLongitude().isNaN();

        // If we have location information, update the flight tracker
        if (hasLatitude && hasLongitude) {

            // Get the nearest airport + distance to the event, and update the moving average
            latestClosestAirport = airports.getClosestAirport(event.getGeoCoordinate());
            Double distance = latestClosestAirport.getGeoCoordinate().getDistanceTo(event.getGeoCoordinate());
            averageDistanceToAirport.ingest(distance, event.getTimestamp().getTime());

            // See if we've recently transition between airborne/not. If so, don't transition again for
            // MIN_TRANSITION_DELAY. This prevents the case where a plane is traveling on the border
            // between the airborne and not.
            Boolean transitionAllowed = event.getTimestamp().getTime() - lastTransitionTime.getTime() > MIN_TRANSITION_DELAY;
            if (airborne != null && !transitionAllowed) return;

            // Determine if the plane is in the air. If we can't determine, just return
            Boolean previouslyAirborne = airborne;
            airborne = isFlightAirborne();
            if (airborne == null) return;

            // If we're on the ground and haven't set our start-airport yet, do so
            if (!airborne && mostRecentlyVisited == null) mostRecentlyVisited = latestClosestAirport;

            // If we've switched from (airborne -> landed) or vice-versa, handle it
            if (previouslyAirborne != null && previouslyAirborne != airborne) {
                lastTransitionTime = event.getTimestamp();
                handleFlightTransition(previouslyAirborne);
            }
        }
    }

    /**
     * Generates a Flight object from the latest available data. If we don't have a
     * recently visited airport, don't set a departure, and if we're still airborne,
     * don't set an arrival.
     */
    public Flight generateFlight() {
        Flight flight = new Flight();
        flight.setAircraftIdentifier(planeId);

        // If we know the last airport we were at, set it as the departure-airport
        if (mostRecentlyVisited != null) {
            flight.setDepartureAirport(mostRecentlyVisited.getIdentifier());
            flight.setDepartureTime(flightStatsTracker.getEarliestCoordinateTime());
        }

        // Determine an arrival airport + time by seeing if the latest event was at an airport
        if (!airborne) {
            GeoCoordinate endCoordinate = flightStatsTracker.getLatestCoordinate();
            Airport closestEndAirport = airports.getClosestAirport(endCoordinate);
            flight.setArrivalAirport(closestEndAirport.getIdentifier());
            flight.setArrivalTime(flightStatsTracker.getLatestTime());
        }

        return flight;
    }

    /**
     * Based on the most recent data, determines if the plane is currently airborne. Criteria are:
     * - Average location in last MOVING_AVERAGE_TIME_WINDOW ms is within AIRPORT_DISTANCE_THRESHOLD of an airport.
     * - Difference between altitude and elevation is less than ALTITUDE_DIFFERENCE_THRESHOLD.
     * - Speed is less than SPEED_THRESHOLD.
     */
    private Boolean isFlightAirborne() {
        // Compute our 3 criteria for being airborne
        Boolean nearAirport = averageDistanceToAirport.getAverageValue() <= AIRPORT_DISTANCE_THRESHOLD;
        Boolean low_altitude = (flightStatsTracker.getLatestAltitude() != null) ?
                flightStatsTracker.getLatestAltitude() - latestClosestAirport.getElevation() < ALTITUDE_DIFFERENCE_THRESHOLD :
                null;
        Boolean low_speed = (flightStatsTracker.getLatestSpeed() != null) ?
                flightStatsTracker.getLatestSpeed() < SPEED_THRESHOLD :
                null;

        // Case 1: If low altitude, return false
        if (low_altitude != null && low_altitude) {
            return false;

        // Case 2: If low speed, return false
        } else if (low_speed != null && low_speed) {
            return false;

        // Case 3: If away from an airport with no low speed/alt info, return true
        } else if (!nearAirport) {
            return true;

        // Case 4: If near an airport with no speed/altitude info, return null, since we don't know
        } else if (low_altitude == null && low_speed == null) {
                return null;

        // Case 5: Near an airport with high speed or altitude, return true
        } else {
            return true;
        }
    }

    /**
     * If the airborne status of the plan has changed, handle starting/stopping the flight.
     * @param previouslyAirborne The state of 'airborne' before the most recently computed value.
     */
    private void handleFlightTransition(Boolean previouslyAirborne) {
        if (previouslyAirborne == airborne) return;

        // If we just went airborne, reset the tracker to 'start' the flight.
        // Note that (previouslyAirborne = null) implies we don't know if we are airborne or grounded yet.
        if (airborne && previouslyAirborne != null && !previouslyAirborne) {
            flightStatsTracker.reset();
        }

        // If we just 'landed' after having been airborne, end the flight, and create the Flight object
        if (!airborne && previouslyAirborne != null && previouslyAirborne) {
            Flight flight = generateFlight();
            flights.add(flight);
            // Update this AFTER generating the flight
            mostRecentlyVisited = latestClosestAirport;
        }
    }
}
