# LUP GTFS Generator

This generator parses the [official pdf schedule](https://www.st-poelten.at/images/Verkehr/LUP_Fahrplanheft_A6_09_2019_I.pdf) into a
machine processable JSON structure. The parsed schedule is then combined with the available routes
from OpenStreetMaps to produce the final feed.

## Download OSM data

```shell script
# download OSM data for austria
$ wget https://download.geofabrik.de/europe/austria-latest.osm.pbf
# crop to the wider St. Pölten area
$ osmconvert austria-latest.osm.pbf -b=15.437878,48.076947,15.841626,48.301253 --complete-ways -o=stp.osm.pbf
```

## Generate feed
```shell script
$ ./gradlew :agency:lup:run  --args="--schedule-path $(pwd)/agency/lup/src/main/resources/2019-09-plan.pdf --osm-path $(pwd)/stp.osm.pbf --output-dir $(pwd)/generated/lup --start-date 2019-12-09 --end-date 2020-12-07"
# check generated/lup for the complete feed

# create the final GTFS feed as zip file
$ zip $(pwd)/generated/lup/lup-gtfs.zip $(pwd)/generated/lup/gtfs/*
```
