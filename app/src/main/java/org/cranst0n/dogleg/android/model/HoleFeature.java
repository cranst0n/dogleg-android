package org.cranst0n.dogleg.android.model;

public class HoleFeature {

  public final String name;
  public final LatLon[] coordinates;
  public final long holeId;

  public HoleFeature(final String name, final LatLon[] coordinates, final long holeId) {
    this.name = name;
    this.coordinates = coordinates;
    this.holeId = holeId;
  }

}
