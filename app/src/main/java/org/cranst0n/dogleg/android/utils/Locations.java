package org.cranst0n.dogleg.android.utils;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class Locations {

  public enum Precision {
    HIGH(TimeUnit.SECONDS.toMillis(10), 10),
    LOW(TimeUnit.MINUTES.toMillis(1), 200);

    Precision(final long timeThreshold, final double distanceThreshold) {
      this.timeThreshold = timeThreshold;
      this.distanceThreshold = distanceThreshold;
    }

    final long timeThreshold;
    final double distanceThreshold;
  }

  private Locations() {

  }

  public static boolean isBetterLocation(@Nullable final Location newLocation,
                                         @Nullable final Location currentLocation,
                                         @NonNull final Precision precision) {

    if (currentLocation == null) {
      return true;
    } else if (newLocation == null) {
      return false;
    }

    // Check whether the new location fix is newer or older
    long timeDelta = newLocation.getTime() - currentLocation.getTime();
    boolean isSignificantlyNewer = timeDelta > precision.timeThreshold;
    boolean isSignificantlyOlder = timeDelta < -precision.timeThreshold;
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
    double accuracyDelta = newLocation.getAccuracy() - currentLocation.getAccuracy();
    boolean isLessAccurate = accuracyDelta > 0;
    boolean isMoreAccurate = accuracyDelta < 0;
    boolean isSignificantlyLessAccurate = accuracyDelta > precision.distanceThreshold;

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

  private static boolean isSameProvider(@Nullable final String provider1,
                                        @Nullable final String provider2) {

    if (provider1 == null) {
      return provider2 == null;
    }
    return provider1.equals(provider2);
  }

}
