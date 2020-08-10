package com.paragonintel.codingexercise;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import com.paragonintel.codingexercise.Airports.AirportCollection;
import com.paragonintel.codingexercise.Events.AdsbEvent;
import com.paragonintel.codingexercise.Flight.Flight;
import com.paragonintel.codingexercise.Flight.PlaneTracker;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main execution class, which performs the following steps:
 * - Loads the event/airport data into data structures.
 * - Processes the events 1 at a time to emulate a stream.
 * - Retrieve the Flights after processing the events.
 * - Save the Flights as JSON to:
 *      CodingExercise/out/production/codingExercise/com/paragonintel/codingexercise/Resources/{FLIGHT_OUTPUT_FILE_NAME}
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    // Could also be passed in as args depending on usage
    private static final String RESOURCE_DIR_PATH = ".\\Resources";  // Resource dir path relative to this class
    private static final String AIRPORT_DATA_FILE_NAME = "airports.json";
    private static final String EVENT_DATA_FILE_NAME = "events.txt";
    private static final String FLIGHT_OUTPUT_FILE_NAME = "flight_results.json";

    /**
     * Performs the primary execution of the Main class, computing and saving values.
     */
    public static void main(String[] args) throws Exception {
        // Step 1: Load the data
        AirportCollection airports = loadAirportCollection(getResourceFilePath(AIRPORT_DATA_FILE_NAME));
        List<AdsbEvent> events = loadEventData(getResourceFilePath(EVENT_DATA_FILE_NAME));

        // Step 2: Process the events sequentially as a stream
        Map<String, PlaneTracker> planeTrackerMap = new HashMap<>();
        for (AdsbEvent event : events) {
            String planeId = event.getIdentifier();
            if (!planeTrackerMap.containsKey(planeId)) {
                planeTrackerMap.put(planeId, new PlaneTracker(planeId, airports));
            }
            planeTrackerMap.get(planeId).processEvent(event);
        }

        // Step 3: Request the Flights from each PlaneTracker
        List<Flight> flights = new ArrayList<>();
        planeTrackerMap.values()
                .stream()
                .forEach(planeTracker -> flights.addAll(planeTracker.getFlights()));

        // Step 4: Output the results as JSON
        Gson serializer = new GsonBuilder().setPrettyPrinting().create();
        saveJsonToFile(
                serializer.toJson(flights),
                getResourceFilePath(FLIGHT_OUTPUT_FILE_NAME)
        );
    }

    /**
     * Loads the data from the specified file path, and instantiates an AirportCollection with it.
     * @param filePath Fully qualified path the the airport data file.
     * @return AirportCollection containing the airports indexed in a KDTree.
     * @throws IOException
     */
    private static AirportCollection loadAirportCollection(String filePath) throws IOException {
        try {
            return AirportCollection.loadFromFile(filePath);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to load airport data from filepath: " + filePath);
            throw ioe;
        }
    }

    /**
     * Reads in the data from the specified filepath, and converts each line to an AdsbEvent.
     * @param filePath Fully qualified path the the event data file.
     * @return List of AdsbEvents loaded from the file.
     * @throws IOException
     */
    private static List<AdsbEvent> loadEventData(String filePath) throws IOException {

        // Step 1: Read the lines from the file
        List<String> fileLines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                fileLines.add(line);
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to load event data from filepath: " + filePath);
            throw ioe;
        } finally {
            if (reader != null) reader.close();
        }

        // Step 2: Convert the Strings to AdsbEvents
        List<AdsbEvent> events = new ArrayList<>();
        for (String line : fileLines) {
            try {
                events.add(AdsbEvent.fromJson(line));
            } catch (JsonSyntaxException jse) {
                logger.log(Level.SEVERE, "Failed to parse AdsbEvent from JSON: " + line);
                throw jse;
            }
        }

        return events;
    }

    /**
     * Generic method for getting a file path from the /Resources directory relative to this file.
     * @param fileName Name of the resource file to construct the path for.
     * @return The fully qualified path to the resource file.
     */
    private static String getResourceFilePath(String fileName) {
        URL main = Main.class.getResource("Main.class");
        String pathString = new File(main.getPath()).toString();
        String mainDirPathString = pathString.substring(0, pathString.lastIndexOf("\\"));
        String resourceFilePath = mainDirPathString + "\\" + RESOURCE_DIR_PATH + "\\" + fileName;
        return resourceFilePath;
    }

    /**
     * Saves the provided payload String to the specified file path.
     * @param jsonPayload String representing the content to be saved.
     * @param filepath Fully qualified path to where the file should be stored.
     * @throws IOException
     */
    private static void saveJsonToFile(String jsonPayload, String filepath) throws IOException {
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(filepath), "utf-8"))) {
            writer.write(jsonPayload);
        }
    }
}
