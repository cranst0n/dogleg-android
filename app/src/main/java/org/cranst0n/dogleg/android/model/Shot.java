package org.cranst0n.dogleg.android.model;

import org.cranst0n.dogleg.android.utils.Units;

public class Shot {

  public final long id;
  public final int sequence;
  public final Club club;
  public final LatLon locationStart;
  public final LatLon locationEnd;
  public final long holeScoreId;

  public Shot(final long id, final int sequence, final Club club, final LatLon locationStart,
              final LatLon locationEnd, final long holeScoreId) {

    this.id = id;
    this.sequence = sequence;
    this.club = club;
    this.locationStart = locationStart;
    this.locationEnd = locationEnd;
    this.holeScoreId = holeScoreId;
  }

  public double distance() {
    if(locationStart != null && locationEnd != null) {
      double meters = locationStart.toLocation().distanceTo(locationEnd.toLocation());
      return Units.metersToYards(meters);
    } else {
      return 0;
    }
  }

  public Shot sequence(final int sequence) {
    return new Shot(id, sequence, club, locationStart, locationEnd, holeScoreId);
  }

  public Shot locationEnd(final LatLon locationEnd) {
    return new Shot(id, sequence, club, locationStart, locationEnd, holeScoreId);
  }
}
