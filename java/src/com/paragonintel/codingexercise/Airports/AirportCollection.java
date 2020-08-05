package com.paragonintel.codingexercise.Airports;

import com.google.gson.Gson;
import com.paragonintel.codingexercise.GeoCoordinate;

import java.io.*;

public class AirportCollection {

    public AirportCollection(Airport[] airports) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }
}
