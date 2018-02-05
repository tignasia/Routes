package com.ti.routes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;


@Slf4j
/**
 * Main class
 */
public class Routes {

  public static final String INPUT_CSV_PATH = "data/DEBRV_DEHAM_historical_routes.csv";
  public static final String OUTPUT_GEO_JSON_FILENAME = "DEBRV_DEHAM_avg_route.geojson";
  public static final int CUT_OFF_DISTANCE_FROM_AVG = 9;

  public static void main(String[] args) throws IOException {
    val path = Paths.get(INPUT_CSV_PATH);
    val inputCsvFile = path.toFile();
    val outputGeoJsonFile = new File(path.toFile().getParentFile(), OUTPUT_GEO_JSON_FILENAME);
    val routes = new Routes();
    routes.calculateAverageRoute(inputCsvFile,outputGeoJsonFile);
  }

  /**
   * Calculates average routes from CSV, calculates
   * @param inputCsvFile
   * @param outputGeoJsonFile
   * @throws IOException
   */
  public  void calculateAverageRoute(File inputCsvFile,File outputGeoJsonFile) throws IOException {

    val routeReader = new CSVRouteReader();
    log.info("Reading routes from {}",inputCsvFile.getAbsolutePath());
    val allRoutes = routeReader.readRoutes(inputCsvFile);
    val minCoordinates = getMinimumNumberOfCoordinates(allRoutes);

    // Calculate avg route cength
    double avgRouteLength  =  calculateRouteAverageLength(allRoutes);

    // Filter out routes of extreme length which are apparent not intentional routes (Intended to different port and then returning back to 2nd port
    // and mistakes (too short routes leading over land)
    // +-25 km cutoff is somewhat arbitrary number to get rid of extremes. In test set  filtering 91.5% routes remain, 8.5% being fibered out

    val filteredRoutes = filterOutExtremeRoutes(allRoutes, avgRouteLength ,avgRouteLength/CUT_OFF_DISTANCE_FROM_AVG);
    log.info("Filtered routes size : {}", filteredRoutes.size());

    // Perform route simplification by reducing number of coordinates in the routes, using minimum number of coordinates as target size for simplification

    val simplifiedCoordinatesMatrix = simplifyCoordinates(minCoordinates, filteredRoutes);

    val coordinateCloud = getCoordinatesAsList(simplifiedCoordinatesMatrix);

    log.info("Coordinate cloud size : {} ", coordinateCloud.size());

    // Generate starting coordinates by averaging (finding central coordinate) of each subsequent coordinate across all simplified routes
    val centralCoordinates = generateInitialCentralCoordinates(simplifiedCoordinatesMatrix);

    // Do k-means clustering using centralCoordinates as initial centroids instead of randomly initializing it to speed up calculation

    long start = System.currentTimeMillis();
    log.info("Calculating average route using clustering (k-means clustering) ... please wait");
    val avgRoute = KMeansClustering.kmeans(centralCoordinates, coordinateCloud);
    log.info("Average route calculation using k-means clustering took {} ms", System.currentTimeMillis() - start);

    // write result GeoJson
    val resultWriter = new GeoJsonResultWriter();
    log.info("Writing average route to {}", outputGeoJsonFile.getAbsolutePath());
    resultWriter.writeResult(avgRoute, outputGeoJsonFile);
  }

  private List<Coordinate2D> getCoordinatesAsList(Coordinate2D[][] simplifiedCoordinatesMatrix) {
    return Arrays.stream(simplifiedCoordinatesMatrix).flatMap(c -> Arrays.stream(c)).collect(Collectors.toList());
  }

  private Coordinate2D[][] simplifyCoordinates(int routeSize,
      Map<RouteInfo, List<Coordinate2D>> filteredRoutes) {
    int numberOfRoutes = filteredRoutes.size();
    Coordinate2D[][] simplifiedCoordinatesMatrix = new Coordinate2D[numberOfRoutes][routeSize];
    int routeIndex = 0;

    for (Entry<RouteInfo, List<Coordinate2D>> entry : filteredRoutes.entrySet()) {
      RouteInfo routeInfo = entry.getKey();
      List<Coordinate2D> coordinates = entry.getValue();
      Coordinate2D[] simplifiedCoordinatesArray = VisvalingamLineSimplifier
          .simplify(coordinates.toArray(new Coordinate2D[0]), routeSize);
      if (simplifiedCoordinatesArray.length < routeSize) {
        log.info("Simplified coordinates of wrong length  : {} for {} ", simplifiedCoordinatesArray.length, routeInfo);

      }
      simplifiedCoordinatesMatrix[routeIndex++] = simplifiedCoordinatesArray;
    }
    return simplifiedCoordinatesMatrix;
  }

  private List<Coordinate2D> generateInitialCentralCoordinates(Coordinate2D[][] simplifiedCoordinatesMatrix) {
    Coordinate2D[][] coordinatesArraySwapped = swapMatrix(simplifiedCoordinatesMatrix);
    List<Coordinate2D> centralCoordinates = new ArrayList<>(coordinatesArraySwapped.length);
    for (Coordinate2D[] coordinates : coordinatesArraySwapped) {
      centralCoordinates.add(Coordinate2D.getCentralGeoCoordinate(coordinates));
    }
    return centralCoordinates;
  }


  private Map<RouteInfo, List<Coordinate2D>> filterOutExtremeRoutes(Map<RouteInfo, List<Coordinate2D>> allRoutes,
      double averageLength, double cutOffDistanceFromAvg) {
    return allRoutes.keySet().stream().
        filter(ri -> (ri.getRouteLength() < averageLength + cutOffDistanceFromAvg) && (ri.getRouteLength()
            > averageLength - cutOffDistanceFromAvg)).
        collect(Collectors.toMap(ri -> ri, ri -> allRoutes.get(ri)));
  }

  private double calculateRouteAverageLength(Map<RouteInfo, List<Coordinate2D>> allRoutes) {
    DoubleSummaryStatistics doubleSummaryStatistics = allRoutes.keySet().stream().mapToDouble(ri -> ri.getRouteLength())
        .summaryStatistics();
    double minDist = doubleSummaryStatistics.getMin();
    double maxDist = doubleSummaryStatistics.getMax();
    double avg = doubleSummaryStatistics.getAverage();
    log.info("Min route distance : {} km ", minDist);
    log.info("Max route distance : {} km", maxDist);
    log.info("Avg route distance : {} km", avg);
    return avg;
  }

  private int getMinimumNumberOfCoordinates(Map<RouteInfo, List<Coordinate2D>> allRoutes) {
    return allRoutes.values().stream().min(Comparator.comparingInt(List::size)).orElseGet(ArrayList::new).size();
  }

  private  Coordinate2D[][] swapMatrix(Coordinate2D[][] coordinatesMatrix) {
    int originalTotalRows = coordinatesMatrix.length;
    int originalTotalColumns = coordinatesMatrix[0].length;
    Coordinate2D[][] newMatrix = new Coordinate2D[originalTotalColumns][originalTotalRows];
    for (int i = 0; i < originalTotalRows; i++) {
      for (int j = 0; j < originalTotalColumns; j++) {
        newMatrix[j][i] = coordinatesMatrix[i][j];
      }
    }
    return newMatrix;
  }


}
