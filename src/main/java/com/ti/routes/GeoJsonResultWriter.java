package com.ti.routes;

import static com.google.common.base.Preconditions.checkArgument;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Coordinates;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.positions.LinearPositions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.val;

/**
 * Implementation of {@link ResultWriter} which contains single public method to write result route to the GeoJson file
 */
public class GeoJsonResultWriter implements ResultWriter {

  @Override
  public void writeResult(List<Coordinate2D> results, File outputFile) throws IOException {

    checkArgument(results != null, "Results cannot be null !");
    checkArgument(outputFile != null, "Output file cannot be null !");

    val resultProperties = ImmutableMap.<String, JsonElement>builder()
        .put("Description", new JsonPrimitive("Avg route"))
        .put("from_seq", new JsonPrimitive(""))
        .put("to_seq", new JsonPrimitive(""))
        .put("stroke-width", new JsonPrimitive("3"))
        .put("stroke", new JsonPrimitive("black")).put(
            "stroke-opacity", new JsonPrimitive(1)).build();

    Feature resultFeature = new Feature(toGeoGson(results), resultProperties, Optional.absent());
    List<Feature> resultFeatures = new ArrayList<>();
    resultFeatures.add(resultFeature);

    FeatureCollection resultFeatureCollection = new FeatureCollection(resultFeatures);

    try (FileWriter fileWriter = new FileWriter(outputFile)) {
      Gson gson = new GsonBuilder()
          .registerTypeAdapterFactory(new GeometryAdapterFactory()).setPrettyPrinting()
          .create();

      gson.toJson(resultFeatureCollection, fileWriter);
      fileWriter.flush();
    }

  }

  private Geometry<?> toGeoGson(List<Coordinate2D> simplifiedCoordinates) {
    return new com.github.filosganga.geogson.model.LineString(
        new LinearPositions(toGeoGsonPositions(simplifiedCoordinates)));
  }

  private ImmutableList<SinglePosition> toGeoGsonPositions(List<Coordinate2D> simplifiedCoordinates) {
    return ImmutableList.copyOf(
        simplifiedCoordinates.stream().map(coordinate -> Coordinates.of(coordinate.getLngt(), coordinate.getLat()))
            .map(SinglePosition::new).collect(Collectors.toList()));
  }
}
