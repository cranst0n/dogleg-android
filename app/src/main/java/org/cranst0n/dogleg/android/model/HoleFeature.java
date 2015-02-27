package org.cranst0n.dogleg.android.model;

public class HoleFeature {

  public final long id;
  public final String name;
  public final LatLon[] coordinates;
  public final long holeId;

  private LatLon center;

  public HoleFeature(final long id, final String name, final LatLon[] coordinates,
                     final long holeId) {

    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
    this.holeId = holeId;
  }

  public synchronized LatLon center() {
    if (center == null) {
      if (coordinates.length == 0) {
        center = new LatLon(0, 0);
      } else {

        double latSum = 0;
        double lonSum = 0;
        double altSum = 0;

        for (LatLon latLon : coordinates) {
          latSum += latLon.latitude;
          lonSum += latLon.longitude;
          altSum += latLon.altitude;
        }

        center = new LatLon(latSum / coordinates.length,
            lonSum / coordinates.length, altSum / coordinates.length);
      }
    }
    return center;
  }
}
