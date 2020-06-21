# OSM GTFS

Toolbox for semi-automatic generation of GTFS feeds using openly available data.

## Goal
GTFS (General Transit Feed Specification) is a widely adopted format for specifying public transit data such as stops, routes and stop times.
Since not all transit service operators provide such a feed there often is no way to get reliable machine-readable data.

However, most transit agencies publish some sort of schedule (often as a PDF) so that travellers know which routes are available at which times.
Furthermore, the community often maintains transit route data in OpenStreetMaps.

This project aims to make it easier to generate user-provided GTFS feeds by combining the above two sources:
* OpenStreetMap data is used to find routes including
  * Route names and colors
  * Stops (coordinates and names)
  * Stop sequence for a given route
  * Path of a route
* Transit agency schedule
  * Timetable
   
## Library
Core of this project is the libray part which provides parsers for OpenStreetMap data and utilities for writing GTFS feeds. Additionally, an interface is provided which implementers can use to provide OpenStreetMap and schedule data. The library will then merge those two together and provide a GTFS feed.

## Feeds
Currently, the following feeds are implemented:
* [LUP (Bus transit agency for the city of St. Pölten, Austria)](agency/lup/README.md)