package com.ti.routes;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class RoutesTest {

  public static final String INPUT_CSV_PATH = "data/DEBRV_DEHAM_historical_routes.csv";
  public static final String OUTPUT_GEO_JSON_FILENAME = "DEBRV_DEHAM_avg_route.geojson";

  @Before
  public void cleanup() {
    val path = Paths.get(INPUT_CSV_PATH);
    val outputGeoJsonFile = new File(path.toFile().getParentFile(), OUTPUT_GEO_JSON_FILENAME);
    outputGeoJsonFile.delete();
    assertFalse(outputGeoJsonFile.exists());
  }

  @Test
  public void testGeneratingAvgRoute() throws IOException {
    val path = Paths.get(INPUT_CSV_PATH);
    val inputCsvFile = path.toFile();
    val outputGeoJsonFile = new File(path.toFile().getParentFile(), OUTPUT_GEO_JSON_FILENAME);
    val routes = new Routes();
    routes.calculateAverageRoute(inputCsvFile, outputGeoJsonFile);
    assertTrue(inputCsvFile.exists());
  }


}
