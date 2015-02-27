package org.cranst0n.dogleg.android.model;

import android.location.Location;

public class LatLon {

  public final double latitude;
  public final double longitude;
  public final double altitude;

  public LatLon(final double latitude, final double longitude) {
    this(latitude, longitude, 0);
  }

  public LatLon(final double latitude, final double longitude, final double altitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.altitude = altitude;
  }

  public float distanceTo(final LatLon latLon) {
    return toLocation().distanceTo(latLon.toLocation()); // meters
  }

  public Location toLocation() {
    Location l = new Location("dogleg");

    l.setLatitude(latitude);
    l.setLongitude(longitude);
    l.setAltitude(altitude);

    return l;
  }

  public static LatLon fromLocation(final Location location) {
    if(location == null) {
      return null;
    } else {
      return new LatLon(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }
  }
}
