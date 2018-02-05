package com.ti.routes;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements k-means clustering for 2d geo coordinates
 */
public class KMeansClustering {

  private KMeansClustering() {
  }

  /**
   * Start with dataset (points in 2d space) and initial cluster centroids (mabe be selected randomly) and then
   * iteratively do cluster assignment step, the algorithm goes through each of the data points and depending
   * on which cluster is closer assigns the data points to one of the cluster centroids. In move centroid step, K-means
   * moves the centroids to the average of the points in a cluster. This process is repeated until there is no change
   * in the clusters (they all converge  and stop changing positions)
   *
   * @param centers initial cluster centroids
   * @param dataset containing 'cloud' of 2D  geo coordinates
   */
  public static List<Coordinate2D> kmeans(List<Coordinate2D> centers, List<Coordinate2D> dataset) {
    boolean converged;

    do {
      // Cluster assignment step
      List<List<Coordinate2D>> clusters = doClusterAssignment(dataset, centers);
      // Move centroid step
      List<Coordinate2D> newCenters = moveCentroids(centers, clusters);
      // calculate distance between old and new centroids
      double dist = Coordinate2D.calculateDistance(centers, newCenters);
      centers = newCenters;
      converged = dist == 0;
    } while (!converged);
    return centers;
  }

  private static List<List<Coordinate2D>> doClusterAssignment(List<Coordinate2D> dataset, List<Coordinate2D> centers) {
    List<List<Coordinate2D>> clusters = new ArrayList<>(centers.size());
    for (int i = 0; i < centers.size(); i++) {
      clusters.add(new ArrayList<>());
    }
    for (Coordinate2D data : dataset) {
      int index = data.getNearestPointIndex(centers);
      clusters.get(index).add(data);
    }
    return clusters;
  }

  private static List<Coordinate2D> moveCentroids(List<Coordinate2D> centers, List<List<Coordinate2D>> clusters) {
    List<Coordinate2D> newCenters = new ArrayList<>(centers.size());
    for (List<Coordinate2D> cluster : clusters) {
      newCenters.add(Coordinate2D.getCentralGeoCoordinate(cluster));
    }
    return newCenters;
  }


}
