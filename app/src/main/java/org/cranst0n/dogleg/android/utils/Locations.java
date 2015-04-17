package org.cranst0n.dogleg.android.utils;

import android.location.Location;

public class Locations {

  private static final int ONE_MINUTE = 1000 * 60;

  private Locations() {

  }

  public static boolean isBetterLocation(final Location newLocation, final Location currentLocation) {

    if (currentLocation == null) {
      // A new location is always better than no location
      return true;
    }

    // Check whether the new location fix is newer or older
    long timeDelta = newLocation.getTime() - currentLocation.getTime();
    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
    boolean isNewer = timeDelta > 0;

    // If it's been more than a minute since the current location, use the new location
    // because the user has likely moved
    if (isSignificantlyNewer) {
      return true;
      // If the new location is more than a minutes old, it must be worse
    } else if (isSignificantlyOlder) {
      return false;
    }

    // Check whether the new location fix is more or less accurate
    int accuracyDelta = (int) (newLocation.getAccuracy() - currentLocation.getAccuracy());
    boolean isLessAccurate = accuracyDelta > 0;
    boolean isMoreAccurate = accuracyDelta < 0;
    boolean isSignificantlyLessAccurate = accuracyDelta > 10;

    // Check if the old and new location are from the same provider
    boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
        currentLocation.getProvider());

    // Determine location quality using a combination of timeliness and accuracy
    if (isMoreAccurate) {
      return true;
    } else if (isNewer && !isLessAccurate) {
      return true;
    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
      return true;
    }

    return false;
  }

  /**
   * Checks whether two providers are the same
   */
  private static boolean isSameProvider(final String provider1, final String provider2) {
    if (provider1 == null) {
      return provider2 == null;
    }
    return provider1.equals(provider2);
  }

}
