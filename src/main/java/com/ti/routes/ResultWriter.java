package com.ti.routes;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface which contains single method to write result route to the file
 */
public interface ResultWriter {

  void writeResult(List<Coordinate2D> result, File outputFile) throws IOException;

}
