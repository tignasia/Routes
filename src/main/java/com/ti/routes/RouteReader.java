package com.ti.routes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface which contains single method to read routes from the file
 */
public interface RouteReader {

  Map<RouteInfo, List<Coordinate2D>> readRoutes(File dataFile) throws IOException;

}
