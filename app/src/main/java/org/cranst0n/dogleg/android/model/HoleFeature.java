package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class HoleFeature {

  public final long id;
  @NonNull
  public final String name;
  @NonNull
  public final LatLon[] coordinates;
  public final long holeId;

  private LatLon center;

  public HoleFeature(final long id, @NonNull final String name, @NonNull final LatLon[] coordinates,
                     final long holeId) {

    this.id = id;
    this.name = name;
    this.coordinates = coordinates;
    this.holeId = holeId;
  }

  @NonNull
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
