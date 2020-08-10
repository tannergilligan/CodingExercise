package com.paragonintel.codingexercise.Airports;

import com.google.gson.Gson;
import com.paragonintel.codingexercise.Location.GeoCoordinate;
import net.sf.javaml.core.kdtree.KDTree;

import java.io.*;

public class AirportCollection {

    KDTree airports;  // Use a KDTree to lookup nearest airport in log(n) time

    public AirportCollection(Airport[] airportArray) {
        airports = new KDTree(2);
        for (Airport airport : airportArray) {
            double[] coordinates = new double[2];
            coordinates[0] = airport.getLatitude();
            coordinates[1] = airport.getLongitude();
            airports.insert(coordinates, airport);
        }
    }

    public static AirportCollection loadFromFile(String filePath) throws IOException {
        var file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        Reader reader = new FileReader(filePath);
        Gson gson = new Gson();
        Airport[] airports = gson.fromJson(reader, Airport[].class);
        return new AirportCollection(airports);
    }

    public Airport getClosestAirport(GeoCoordinate coordinate) {
        double[] latLongCoordinate = new double[2];
        latLongCoordinate[0] = coordinate.getLatitude();
        latLongCoordinate[1] = coordinate.getLongitude();
        return (Airport) airports.nearest(latLongCoordinate);
    }
}
