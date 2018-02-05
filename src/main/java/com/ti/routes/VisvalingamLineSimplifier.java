package com.ti.routes;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Simplifies a poly-line (sequence of points) using  Visvalingam algorithm which
 * simplifies geometry by removing lines while trying to minimize the area changed.
 */
@RequiredArgsConstructor
class VisvalingamLineSimplifier {

  public static Coordinate2D[] simplify(Coordinate2D[] pts, int numberOfPoints) {
    VisvalingamLineSimplifier simp = new VisvalingamLineSimplifier(pts, numberOfPoints);
    return simp.simplify();
  }

  private final Coordinate2D[] pts;
  private final int numberOfPoints;

  public Coordinate2D[] simplify() {
    // do not simplify already "simple" lines
    if (pts.length <= numberOfPoints) {
      return pts;
    }

    LineSegment lineSegment = LineSegment.buildPolyline(pts);
    int numOfPoints = 0;
    do {
      numOfPoints = simplifyPolyline(lineSegment);
    } while (numberOfPoints < numOfPoints);

    Coordinate2D[] simp = lineSegment.getCoordinates();
    if (simp.length < 2) {
      return new Coordinate2D[]{simp[0], new Coordinate2D(simp[0])};
    }
    return simp;
  }

  /**
   * Iterate over lines constituting polyline and remove the one with smallest area
   */
  private int simplifyPolyline(LineSegment pLineSegment) {
    LineSegment curr = pLineSegment;
    double minArea = curr.getArea();
    LineSegment minLineSegment = null;
    List<Coordinate2D> coords = new ArrayList<>();
    while (curr != null) {
      double area = curr.getArea();
      if (area < minArea) {
        minArea = area;
        minLineSegment = curr;
      }
      coords.add(curr.pt);
      curr = curr.next;
    }
    if (minLineSegment != null && coords.size() > numberOfPoints) {
      minLineSegment.remove();
      coords.remove(minLineSegment.pt);
    }

    if (!pLineSegment.isLive()) {
      return -1;
    }

    return coords.size();
  }


  @RequiredArgsConstructor
  @Getter
  private static class LineSegment {

    private final Coordinate2D pt;
    @Setter
    private LineSegment prev;
    @Setter
    private LineSegment next;
    private double area = Double.MAX_VALUE;
    private boolean isLive = true;

    public static LineSegment buildPolyline(Coordinate2D[] pts) {
      LineSegment first = null;
      LineSegment prev = null;
      for (int i = 0; i < pts.length; i++) {
        LineSegment ls = new LineSegment(pts[i]);
        if (first == null) {
          first = ls;
        }
        ls.setPrev(prev);
        if (prev != null) {
          prev.setNext(ls);
          prev.updateArea();
        }
        prev = ls;
      }
      return first;
    }


    private void updateArea() {
      if (prev == null || next == null) {
        area = Double.MAX_VALUE;
        return;
      }
      area = triangleArea(prev.pt, pt, next.pt);
    }

    private LineSegment remove() {
      LineSegment tmpPrev = prev;
      LineSegment tmpNext = next;
      LineSegment result = null;
      if (prev != null) {
        prev.setNext(tmpNext);
        prev.updateArea();
        result = prev;
      }
      if (next != null) {
        next.setPrev(tmpPrev);
        next.updateArea();
        if (result == null) {
          result = next;
        }
      }
      isLive = false;
      return result;
    }

    private Coordinate2D[] getCoordinates() {
      List<Coordinate2D> coords = new ArrayList<>();
      LineSegment curr = this;
      do {
        coords.add(curr.pt);
        curr = curr.next;
      } while (curr != null);
      return coords.toArray(new Coordinate2D[0]);
    }


    private double triangleArea(Coordinate2D a, Coordinate2D b, Coordinate2D c) {
      return abs(
          ((c.getLngt() - a.getLngt()) * (b.getLat() - a.getLat()) - (b.getLngt() - a.getLngt()) * (c.getLat() - a
              .getLat())) / 2);
    }
  }


}