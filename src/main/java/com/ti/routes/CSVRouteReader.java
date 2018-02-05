package com.ti.routes;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * Implementation of {@link RouteReader} which contains single public method to read routes from csv file
 */
public class CSVRouteReader implements RouteReader {


  private static final Pattern regex = Pattern
      .compile("\\[?([0-9]*[.]?[0-9]+),\\s?([0-9]*[.]?[0-9]+),\\s[0-9]+,\\s?[0-9]*[.]?[0-9]+\\]");

  @Override
  public  Map<RouteInfo, List<Coordinate2D>> readRoutes(File dataFile) throws IOException {
    Map<RouteInfo, List<Coordinate2D>> allRoutes = new LinkedHashMap<>();
    try (CSVParser parser = CSVParser.parse(dataFile, Charset.forName("UTF8"), CSVFormat.RFC4180)) {
      for (CSVRecord record : parser) {
        String vesselId = record.get(0);
        String from_seq = record.get(1);
        String to_seq = record.get(2);
        String from = record.get(3);
        String to = record.get(4);
        String pointsStr = record.get(7);
        List<Coordinate2D> coordinates = extractCoordinates(pointsStr);

        int count = coordinates.size();
        if (count > 0) {
          // revert coordinates - this is for initial cluster centers locations calculated by mean routeLength
          if (from.equals("DEBRV")) {
            coordinates = Lists.reverse(coordinates);
          }
          allRoutes.put(new RouteInfo(vesselId, from_seq, to_seq, Coordinate2D.calculateRouteLength(coordinates)), coordinates);
        }
      }
    }
    return allRoutes;
  }

  private static List<Coordinate2D> extractCoordinates(String pointsStr) {
    List<Coordinate2D> coordinates = new ArrayList<>();
    Matcher regexMatcher = regex.matcher(pointsStr);
    while (regexMatcher.find()) {
      double longitude = Double.parseDouble(regexMatcher.group(1));
      double latitude = Double.parseDouble(regexMatcher.group(2));
      coordinates.add(new Coordinate2D(longitude, latitude));
    }
    return coordinates;
  }
}
