package org.cranst0n.dogleg.android.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

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

  public float distanceTo(final Location location) {
    return toLocation().distanceTo(location);
  }

  public float distanceTo(final LatLng latLng) {
    if (latLng == null) {
      return Float.MAX_VALUE;
    } else {
      return fromLatLng(latLng).distanceTo(this);
    }
  }

  public Location toLocation() {
    Location l = new Location("dogleg");

    l.setLatitude(latitude);
    l.setLongitude(longitude);
    l.setAltitude(altitude);

    return l;
  }

  public LatLng toLatLng() {
    return new LatLng(latitude, longitude);
  }

  public static LatLon fromLatLng(final LatLng latLng) {
    if (latLng == null) {
      return null;
    } else {
      return new LatLon(latLng.latitude, latLng.longitude);
    }
  }

  public static LatLon fromLocation(final Location location) {
    if (location == null) {
      return null;
    } else {
      return new LatLon(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }
  }
}
