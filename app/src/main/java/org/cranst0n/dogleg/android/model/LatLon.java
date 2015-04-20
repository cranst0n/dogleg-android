package org.cranst0n.dogleg.android.model;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

  public float distanceTo(@NonNull final LatLon latLon) {
    return toLocation().distanceTo(latLon.toLocation()); // meters
  }

  public float distanceTo(@NonNull final Location location) {
    return toLocation().distanceTo(location);
  }

  public float distanceTo(@Nullable final LatLng latLng) {
    if (latLng == null) {
      return Float.MAX_VALUE;
    } else {
      return fromLatLng(latLng).distanceTo(this);
    }
  }

  @NonNull
  public Location toLocation() {
    Location l = new Location("dogleg");

    l.setLatitude(latitude);
    l.setLongitude(longitude);
    l.setAltitude(altitude);

    return l;
  }

  @NonNull
  public LatLng toLatLng() {
    return new LatLng(latitude, longitude);
  }

  @Nullable
  public static LatLon fromLatLng(@Nullable final LatLng latLng) {
    if (latLng == null) {
      return null;
    } else {
      return new LatLon(latLng.latitude, latLng.longitude);
    }
  }

  @Nullable
  public static LatLon fromLocation(@Nullable final Location location) {
    if (location == null) {
      return null;
    } else {
      return new LatLon(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }
  }
}
