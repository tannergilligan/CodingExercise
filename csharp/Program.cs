using ParagonCodingExercise.Airports;
using System;

namespace ParagonCodingExercise
{
    class Program
    {
        private static string AirportsFilePath = @".\Resources\airports.json";

        // Location of ADS-B events
        private static string AdsbEventsFilePath = @".\Resources\events.txt";

        // Write generated flights here
        private static string OutputFilePath = @".\Resources\flights.txt";

        static void Main(string[] args)
        {
            Execute();

            Console.ReadKey();
        }

        private static void Execute()
        {
            // Load the airports
            AirportCollection airports = AirportCollection.LoadFromFile(AirportsFilePath);

        }
    }
}
