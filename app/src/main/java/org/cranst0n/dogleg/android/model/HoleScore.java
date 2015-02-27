package org.cranst0n.dogleg.android.model;

public class HoleScore implements Comparable<HoleScore> {

  public final long id;
  public final long roundId;
  public final int score;
  public final int netScore;
  public final int putts;
  public final int penaltyStrokes;
  public final boolean fairwayHit;
  public final boolean gir;
  public final Hole hole;

  public HoleScore(final long id, final long roundId, final int score, final int netScore,
                   final int putts, final int penaltyStrokes, final boolean fairwayHit,
                   final boolean gir, final Hole hole) {

    this.id = id;
    this.roundId = roundId;
    this.score = score;
    this.netScore = netScore;
    this.putts = putts;
    this.penaltyStrokes = penaltyStrokes;
    this.fairwayHit = fairwayHit;
    this.gir = gir;
    this.hole = hole;
  }

  public static HoleScore empty() {
    return new HoleScore(-1, -1, 0, 0, 0, 0, false, false, Hole.empty());
  }

  public final HoleScore score(final int score) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir, hole);
  }

  public final HoleScore addStroke() {
    return new HoleScore(id, roundId, score + 1, netScore, putts, penaltyStrokes, fairwayHit, gir, hole);
  }

  public final HoleScore subtractStroke() {
    if (score > 0) {
      return new HoleScore(id, roundId, score - 1, netScore, putts, penaltyStrokes, fairwayHit, gir, hole);
    } else {
      return this;
    }
  }

  public final HoleScore putts(final int putts) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir, hole);
  }

  public final HoleScore addPutt() {
    return new HoleScore(id, roundId, score, netScore, putts + 1, penaltyStrokes, fairwayHit, gir, hole);
  }

  public final HoleScore subtractPutt() {
    if (putts > 0) {
      return new HoleScore(id, roundId, score, netScore, putts - 1, penaltyStrokes, fairwayHit, gir, hole);
    } else {
      return this;
    }
  }

  public final HoleScore penaltyStrokes(final int penaltyStrokes) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gir, hole);
  }

  public final HoleScore addPenaltyStroke() {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes + 1, fairwayHit, gir, hole);
  }

  public final HoleScore subtractPenaltyStroke() {
    if (penaltyStrokes > 0) {
      return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes - 1, fairwayHit, gir, hole);
    } else {
      return this;
    }
  }

  public final HoleScore fairwayHit(final boolean fh) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fh, gir, hole);
  }

  public final HoleScore gir(final boolean gr) {
    return new HoleScore(id, roundId, score, netScore, putts, penaltyStrokes, fairwayHit, gr, hole);
  }

  @Override
  public int compareTo(final HoleScore another) {
    return hole.number - another.hole.number;
  }

  public static String scoreToParString(final int score, final int par) {

    if (score == 1) {
      return "Ace";
    }

    int diff = score - par;

    switch (diff) {
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
    }

    return "Other";
  }
}
