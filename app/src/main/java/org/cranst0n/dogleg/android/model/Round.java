package org.cranst0n.dogleg.android.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Round {

  public final long id;
  public final User user;
  public final Course course;
  public final CourseRating rating;
  public final DateTime time;
  public final boolean official;
  public final int handicap;
  public final boolean isHandicapOverridden;
  public final int handicapOverride;

  private final HoleScore[] holeScores;

  private HoleSet holeSet;

  private Round(final long id, final User user, final Course course, final CourseRating rating, final DateTime time,
                final boolean official, final int handicap, final boolean isHandicapOverridden,
                final int handicapOverride, final HoleScore[] holeScores) {

    this.id = id;
    this.user = user;
    this.course = course;
    this.rating = rating;
    this.time = time;
    this.official = official;
    this.handicap = handicap;
    this.isHandicapOverridden = isHandicapOverridden;
    this.handicapOverride = handicapOverride;

    this.holeScores = holeScores;

    Arrays.sort(this.holeScores);
  }

  public int numHoles() {
    return holeSet().numHoles;
  }

  public double roundRating() {
    return rating.rating(holeSet());
  }

  public double roundSlope() {
    return rating.slope(holeSet());
  }

  public HoleScore[] holeScores() {
    return Arrays.copyOf(holeScores, holeScores.length);
  }

  public HoleScore holeScore(final int holeNumber) {
    for (HoleScore holeScore : holeScores) {
      if (holeScore.hole.number == holeNumber) {
        return holeScore;
      }
    }

    return null;
  }

  public HoleScore updateScore(final HoleScore holeScore) {

    HoleScore handicappedScore = handicapScore(holeScore);

    int ix = holeScore.hole.number - holeSet().holeStart;
    holeScores[ix] = handicappedScore;

    return handicappedScore;
  }

  public Round handicapped() {
    for (HoleScore holeScore : holeScores()) {
      updateScore(handicapScore(holeScore));
    }

    return this;
  }

  public HoleScore handicapScore(final HoleScore holeScore) {
    HoleRating holeRating = rating.holeRating(holeScore.hole.number);
    int handicapToUse = handicap();

    // If you're playing 9 holes of an 18 hole course, you need to divide each holes
    // true handicap by 2 to apply stokes to the appropriate holes
    int ratingCorrection = course.numHoles / holeSet().numHoles;

    int netCorrection =
        (int) Math.max(Math.ceil((handicapToUse - Math.ceil((double) holeRating.handicap / ratingCorrection) + 1) / numHoles()), 0);

    return holeScore.netScore(holeScore.score - netCorrection);
  }

  public Round asUser(final User user) {
    return new Round(id, user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  public Round withTime(final DateTime time) {
    return new Round(id, user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  public Round withAutoHandicap(final int handicap) {
    return new Round(id, user, course, rating, time, official,
        handicap, false, handicapOverride, holeScores).handicapped();
  }

  public synchronized HoleSet holeSet() {
    if (holeSet == null) {
      if (holeScores[0].hole.number == 10) {
        holeSet = HoleSet.Back9;
      } else if (holeScores[holeScores.length - 1].hole.number == 18) {
        holeSet = HoleSet.All;
      } else {
        holeSet = HoleSet.Front9;
      }
    }

    return holeSet;
  }

  public boolean hasHandicap() {
    return isHandicapOverridden || handicap > 0;
  }

  public int handicap() {
    return hasHandicap() ? (isHandicapOverridden ? handicapOverride : handicap) : 0;
  }

  public RoundStats stats() {
    return new RoundStats(this);
  }

  public List<RoundIssue> issues() {
    List<RoundIssue> issues = new ArrayList<>();

    for (final HoleScore holeScore : holeScores) {
      if (holeScore.score == 0) {
        issues.add(new RoundIssue(holeScore.hole, String.format("Hole %d has a score of 0.", holeScore.hole.number)));
      }
      if (holeScore.putts >= holeScore.score) {
        issues.add(new RoundIssue(holeScore.hole, String.format("Hole %d has more putts than total score.", holeScore.hole.number)));
      }
    }

    return issues;
  }

  public static Round create(final long id, final User user, final Course course,
                             final HoleSet holeSet) {
    return create(id, user, course, course.ratings[0], DateTime.now(),
        true, 0, false, 0, null, holeSet);
  }

  public static Round create(final long id, final User user, final Course course,
                             final CourseRating rating, final DateTime time, final boolean official,
                             final int handicap, final boolean isHandicapOverridden,
                             final int handicapOverride, final HoleScore[] oldHoleScores,
                             final HoleSet holeSet) {

    Arrays.sort(course.holes);

    HoleScore[] newHoleScores = new HoleScore[holeSet.numHoles];

    if (oldHoleScores == null) {
      for (int holeNum = holeSet.holeStart; holeNum <= holeSet.holeEnd; holeNum++) {
        newHoleScores[holeNum - holeSet.holeStart] =
            new HoleScore(-1, -1, 0, 0, 0, 0,
                rating.holeRating(holeNum).par > 3, true, course.holes[holeNum - 1]);
      }
    } else {

      for (int holeNum = holeSet.holeStart; holeNum <= holeSet.holeEnd; holeNum++) {

        HoleScore oldHoleScore = null;

        for (HoleScore old : oldHoleScores) {
          if (old.hole.number == holeNum) {
            oldHoleScore = old;
          }
        }

        // Use the existing holeFeature score so we don't throw away information
        if (oldHoleScore == null) {
          newHoleScores[holeNum - holeSet.holeStart] =
              new HoleScore(-1, -1, 0, 0, 0, 0,
                  rating.holeRating(holeNum).par > 3, true, course.holes[holeNum - 1]);
        } else {
          newHoleScores[holeNum - holeSet.holeStart] = oldHoleScore;
        }
      }
    }

    return new Round(id, user, course, rating, time, official, handicap, isHandicapOverridden,
        handicapOverride, newHoleScores).handicapped();
  }

  public static class RoundIssue {

    public final Hole hole;
    public final String message;

    public RoundIssue(final Hole hole, final String message) {
      this.hole = hole;
      this.message = message;
    }
  }
}
