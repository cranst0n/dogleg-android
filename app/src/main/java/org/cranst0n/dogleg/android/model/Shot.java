package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

import org.cranst0n.dogleg.android.utils.Units;

public class Shot {

  public final long id;
  public final int sequence;
  @NonNull
  public final Club club;
  @NonNull
  public final LatLon locationStart;
  @NonNull
  public final LatLon locationEnd;
  public final long holeScoreId;

  public Shot(final long id, final int sequence, @NonNull final Club club,
              @NonNull final LatLon locationStart, @NonNull final LatLon locationEnd,
              final long holeScoreId) {

    this.id = id;
    this.sequence = sequence;
    this.club = club;
    this.locationStart = locationStart;
    this.locationEnd = locationEnd;
    this.holeScoreId = holeScoreId;
  }

  public double distanceYards() {
    return Units.metersToYards(distanceMeters());
  }

  public double distanceFeet() {
    return Units.metersToFeet(distanceMeters());
  }

  public double distanceMeters() {
    if (locationStart != null && locationEnd != null) {
      return locationStart.toLocation().distanceTo(locationEnd.toLocation());
    } else {
      return 0;
    }
  }

  @NonNull
  public Shot sequence(final int sequence) {
    return new Shot(id, sequence, club, locationStart, locationEnd, holeScoreId);
  }

  @NonNull
  public Shot locationEnd(@NonNull final LatLon locationEnd) {
    return new Shot(id, sequence, club, locationStart, locationEnd, holeScoreId);
  }
}
