package com.ti.routes;


import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;


@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Coordinate2D {

  private final static double AVERAGE_EARTH_RADIUS = 6371.230d;

  /**
   * The longitude.
   */
  private final double lngt;
  /**
   * The latitude.
   */
  private final double lat;


  public Coordinate2D(Coordinate2D coordinate) {
    this(coordinate.lngt, coordinate.lat);
  }

  /**
   *
   * @param centers
   * @return
   */
  public int getNearestPointIndex(List<Coordinate2D> centers) {
    checkArgument(centers != null, "centers cannot be null !");
    int bestIndex = 0;
    double closest = Double.POSITIVE_INFINITY;
    for (int i = 0; i < centers.size(); i++) {
      val tempDist = calculateDistance(centers.get(i));
      if (tempDist < closest) {
        closest = tempDist;
        bestIndex = i;
      }
    }
    return bestIndex;
  }

  /**
   * Calculates length of the route in kilometers by calculating distances between between subsequent geo coordinates
   * using Haversine formula
   *
   * @param route list of geo coordinates representing route
   * @return length of the route in kilometers
   */
  public static double calculateRouteLength(List<Coordinate2D> route) {
    checkArgument(route != null, "route cannot be null !");
    double routeLength = 0d;
    Coordinate2D prev = null;
    for (Coordinate2D coordinate2D : route) {
      if (prev != null) {
        routeLength += prev.calculateDistance(coordinate2D);
      }
      prev = coordinate2D;
    }
    return routeLength;
  }

  /**
   * Calculates distance between two list of geo coordinates of the same size as sum of distances between
   * coordinates in the same position in the list done using Haversine formula
   */
  public static double calculateDistance(List<Coordinate2D> coordinates1, List<Coordinate2D> coordinates2) {
    checkArgument(coordinates1 != null, "coordinates1 cannot be null !");
    checkArgument(coordinates2 != null, "coordinates2 cannot be null !");
    checkArgument(coordinates1.size() == coordinates2.size(), "Both list of coordinates must be of the same size!");
    double sumDist = 0;
    for (int i = 0; i < coordinates1.size(); i++) {
      double dist = coordinates1.get(i).calculateDistance(coordinates2.get(i));
      sumDist += dist;
    }
    return sumDist;
  }

  /**
   * Calculates central geo coordinate for list of coordinates using spherical law of cosines
   *
   * @param coordinates array of geo coordinates
   * @return central geo coordinate
   */
  public static Coordinate2D getCentralGeoCoordinate(Coordinate2D[] coordinates) {
    checkArgument(coordinates != null, "coordinates cannot be null !");
    return getCentralGeoCoordinate(Arrays.asList(coordinates));
  }

  /**
   * Calculates central geo coordinate for list of coordinates using spherical law of cosines
   *
   * @param coordinates list of geo coordinates
   * @return central geo coordinate
   */
  public static Coordinate2D getCentralGeoCoordinate(List<Coordinate2D> coordinates) {
    checkArgument(coordinates != null, "coordinates cannot be null !");

    if (coordinates.size() == 1) {
      return coordinates.get(0);
    }

    double x = 0;
    double y = 0;
    double z = 0;

    for (val coordinate : coordinates) {
      // convert to radians
      val longitude = toRadians(coordinate.lngt);
      val latitude = toRadians(coordinate.lat);

      x += cos(latitude) * cos(longitude);
      y += cos(latitude) * sin(longitude);
      z += sin(latitude);
    }

    val total = coordinates.size();

    double xAvg = x / total;
    double yAvg = y / total;
    double zAvg = z / total;

    val centralLongitude = atan2(yAvg, xAvg);
    val centralSquareRoot = sqrt(xAvg * xAvg + yAvg * yAvg);
    val centralLatitude = atan2(zAvg, centralSquareRoot);
    // convert back to degrees
    return new Coordinate2D(toDegrees(centralLongitude), toDegrees(centralLatitude));
  }


  /**
   * Calculate distance between this and other geo coordinate in kilometers using Haversine formula which
   * determines the great-circle distance between two points on a sphere given their longitudes
   * and latitudes
   *
   * @param otherCoordinate other geo coordinate
   * @return distance between this and other geo coordinate in kilometers
   * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
   */
  private double calculateDistance(Coordinate2D otherCoordinate) {
    double latDistance = toRadians(this.lat - otherCoordinate.lat);
    double lngDistance = toRadians(this.lngt - otherCoordinate.lngt);
    double a = sin(latDistance / 2) * sin(latDistance / 2)
        + cos(toRadians(this.lat)) * cos(toRadians(otherCoordinate.lat))
        * sin(lngDistance / 2) * sin(lngDistance / 2);

    double c = 2 * atan2(sqrt(a), sqrt(1 - a));

    return AVERAGE_EARTH_RADIUS * c;
  }


}