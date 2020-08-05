This is the Paragon Intel coding and design exercise.

Your task is to create a program that can accept a stream of collected ADS-B events and extract the probable flights from that stream.

ADS-B signals are broadcast by planes and report position and other information at some (relatively frequent) interval. This signal is transmitted in the clear and can be picked up by receivers made from off-the-shelf parts. There is a network of such receivers from which events are centrally collected.

A flight should consist of an aircraft identifier, departure time, arrival time, departure airport and arrival airport.

# Inputs

airports.json: contains JSON array of airport information, with the following fields:
* Latitude
* Longitude
* Elevation
* Identifier

events.txt: JSON objects representing ADS-B events (one event per line), with the following fields:
* Latitude
* Longitude
* Altitude
* Speed
* Heading
* Aircraft identifier
 
The meaning of the fields in each file should be relatively clear from the naming. This is a small sample (with more meaningful property names) of the data that normally would arrive via a TCP connection.

# Output

List of flights, which should have the following per record:

* Aircraft identifier
* Departure time
* Departure airport identifier
* Arrival time
* Arrival airport identifier

Output can be csv, JSON or anything else that makes sense and would be machine readable.

# General notes

* ADS-B signals are line of sight and have a finite range; it is possible to lose the signal for aircraft if it flies out of coverage, over the ocean, behind a mountain, etc.
* Given the previous note, it may be possible to deduce that a plane has likely landed, but not be able to determine where it landed and/or it may be possible to deduce that a plane is in the sky but not be able to determine where it departed. Leaving the airport null or blank for those cases is acceptable.
* For this exercise, assume that any aircraft can land at any airport.
* As the data is provided by a network of ADS-B receivers with no central time synchronization, it is possible that a seemingly nonsensical sequence of events appears in the data stream, that is, it may appear that an aircraft moves backwards or makes other such unrealistic movements.
