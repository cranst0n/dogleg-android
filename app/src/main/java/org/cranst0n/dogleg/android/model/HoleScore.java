package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class HoleScore implements Comparable<HoleScore> {

  public final long id;
  public final long roundId;
  public final int score;
  public final int netScore;
  public final int putts;
  public final int penaltyStrokes;
  public final boolean fairwayHit;
  public final boolean gir;
  @NonNull
  public final List<Shot> shots;
  @NonNull
  public final Hole hole;

  public HoleScore(final long id, final long roundId, final int score, final int netScore,
                   final int putts, final int penaltyStrokes, final boolean fairwayHit,
                   final boolean gir, @NonNull final List<Shot> shots, @NonNull final Hole hole) {

    this.id = id;
    this.roundId = roundId;
    this.score = score;
    this.netScore = netScore;
    this.putts = putts;
    this.penaltyStrokes = penaltyStrokes;
    this.fairwayHit = fairwayHit;
    this.gir = gir;
    this.shots = shots;
    this.hole = hole;
  }

  @NonNull
  public static HoleScore empty() {
    return new HoleScore(-1, -1, 0, 0, 0, 0, false, false, new ArrayList<Shot>(), Hole.empty());
  }

  @NonNull
  public final HoleScore score(final int score) {
    return new HoleScore(id, roundId, Math.max(score, 0), netScore, putts, penaltyStrokes,
        fairwayHit, gir, shots, hole);
  }

  @NonNull
  public final HoleScore netScore(final int netScore) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir,
        shots, hole);
  }

  @NonNull
  public final HoleScore addStroke() {
    return score(score + 1);
  }

  @NonNull
  public final HoleScore subtractStroke() {
    return score(score - 1);
  }

  @NonNull
  public final HoleScore putts(final int putts) {
    return new HoleScore(id, roundId, score, netScore, Math.max(putts, 0), penaltyStrokes,
        fairwayHit, gir, shots, hole);
  }

  @NonNull
  public final HoleScore addPutt() {
    return putts(putts + 1);
  }

  @NonNull
  public final HoleScore subtractPutt() {
    return putts(putts - 1);
  }

  @NonNull
  public final HoleScore penaltyStrokes(final int penaltyStrokes) {
    return new HoleScore(id, roundId, score, netScore, putts, Math.max(penaltyStrokes, 0),
        fairwayHit, gir, shots, hole);
  }

  @NonNull
  public final HoleScore addPenaltyStroke() {
    return penaltyStrokes(penaltyStrokes + 1);
  }

  @NonNull
  public final HoleScore subtractPenaltyStroke() {
    return penaltyStrokes(penaltyStrokes - 1);
  }

  @NonNull
  public final HoleScore fairwayHit(final boolean fh) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fh, gir, shots, hole);
  }

  @NonNull
  public final HoleScore gir(final boolean gr) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gr,
        shots, hole);
  }

  @NonNull
  public final HoleScore withShot(@NonNull final Shot shot) {
    List<Shot> newShots = new ArrayList<>(shots);
    newShots.add(shot);

    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir,
        validatedShots(newShots, false), hole);
  }

  @NonNull
  public final HoleScore removeShot(@NonNull final Shot shot) {
    List<Shot> newShots = new ArrayList<>(shots);
    newShots.remove(shot);

    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir,
        validatedShots(newShots, true), hole);
  }

  @NonNull
  private List<Shot> validatedShots(@NonNull final List<Shot> shots, final boolean shotRemoved) {

    List<Shot> validated = new ArrayList<>();

    for (int ix = 0; ix < shots.size(); ix++) {

      Shot sequenced = shots.get(ix).sequence(ix + 1);

      if (ix < shots.size() - 1) {
        validated.add(sequenced.locationEnd(shots.get(ix + 1).locationStart));
      } else if (shotRemoved) {
        validated.add(sequenced.locationEnd(sequenced.locationStart));
      } else {
        validated.add(sequenced);
      }
    }

    return validated;
  }

  @Override
  public int compareTo(@NonNull final HoleScore another) {
    return hole.number - another.hole.number;
  }

  @NonNull
  public static String scoreToParString(final int score, final int par) {

    if (score == 1) {
      return "Ace";
    }

    switch (score - par) {
      case -4: {
        return "You're a liar";
      }
      case -3: {
        return "Double Eagle";
      }
      case -2: {
        return "Eagle";
      }
      case -1: {
        return "Birdie";
      }
      case 0: {
        return "Par";
      }
      case 1: {
        return "Bogey";
      }
      case 2: {
        return "Double Bogey";
      }
      case 3: {
        return "Triple Bogey";
      }
      case 4: {
        return "Quadruple Bogey";
      }
      default: {
        return "Other";
      }
    }
  }
}
