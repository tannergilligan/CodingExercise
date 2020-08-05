package com.paragonintel.codingexercise;

import java.util.Date;

public class Flight {
    private String aircraftIdentifier;
    private Date departureTime;
    private String departureAirport;
    private Date arrivalTime;
    private String arrivalAirport;

    public String getAircraftIdentifier() {
        return this.aircraftIdentifier;
    }

    public void setAircraftIdentifier(String aircraftIdentifier) {
        this.aircraftIdentifier = aircraftIdentifier;
    }

    public Date getDepartureTime() {
        return this.departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public String getDepartureAirport() {
        return this.departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public Date getArrivalTime() {
        return this.arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getArrivalAirport() {
        return this.arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }
}
