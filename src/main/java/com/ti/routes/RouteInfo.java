package com.ti.routes;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@EqualsAndHashCode(exclude = "routeLength")
@RequiredArgsConstructor
@ToString
public  class RouteInfo {

  private final String vesselId;
  private final String from_seq;
  private final String to_seq;
  private final  double routeLength;
}
