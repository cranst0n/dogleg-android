package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Round {

  public final long id;
  @NonNull
  public final User user;
  @NonNull
  public final Course course;
  @NonNull
  public final CourseRating rating;
  @NonNull
  public final DateTime time;
  public final boolean official;
  public final int handicap;
  public final boolean isHandicapOverridden;
  public final int handicapOverride;

  @NonNull
  private final HoleScore[] holeScores;

  private HoleSet holeSet;

  private Round(final long id, @NonNull final User user, @NonNull final Course course,
                @NonNull final CourseRating rating, @NonNull final DateTime time,
                final boolean official, final int handicap, final boolean isHandicapOverridden,
                final int handicapOverride, @NonNull final HoleScore[] holeScores) {

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

  @NonNull
  public HoleScore[] holeScores() {
    return Arrays.copyOf(holeScores, holeScores.length);
  }

  @Nullable
  public HoleScore holeScore(final int holeNumber) {
    for (HoleScore holeScore : holeScores) {
      if (holeScore.hole.number == holeNumber) {
        return holeScore;
      }
    }

    return null;
  }

  @NonNull
  public HoleScore updateScore(@NonNull final HoleScore holeScore) {

    HoleScore handicappedScore = handicapScore(holeScore);

    int ix = holeScore.hole.number - holeSet().holeStart;
    holeScores[ix] = handicappedScore;

    return handicappedScore;
  }

  @NonNull
  public Round handicapped() {
    for (HoleScore holeScore : holeScores()) {
      updateScore(handicapScore(holeScore));
    }

    return this;
  }

  @NonNull
  public HoleScore handicapScore(@NonNull final HoleScore holeScore) {

    HoleRating holeRating = rating.holeRating(holeScore.hole.number);

    if(holeRating != null) {
      int handicapToUse = handicap();

      // If you're playing 9 holes of an 18 hole course, you need to divide each holes
      // true handicap by 2 to apply stokes to the appropriate holes
      int ratingCorrection = course.numHoles / holeSet().numHoles;

      int netCorrection =
          (int) Math.max(Math.ceil((handicapToUse - Math.ceil((double) holeRating.handicap / ratingCorrection) + 1) / numHoles()), 0);

      return holeScore.netScore(holeScore.score - netCorrection);
    } else {
      return holeScore;
    }
  }

  @NonNull
  public Round asUser(@NonNull final User user) {
    return new Round(id, user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  @NonNull
  public Round withTime(@NonNull final DateTime time) {
    return new Round(id, user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  @NonNull
  public Round withAutoHandicap(final int handicap) {
    return new Round(id, user, course, rating, time, official,
        handicap, false, handicapOverride, holeScores).handicapped();
  }

  @NonNull
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

  @NonNull
  public RoundStats stats() {
    return new RoundStats(this);
  }

  @NonNull
  public List<RoundIssue> issues() {
    List<RoundIssue> issues = new ArrayList<>();

    for (final HoleScore holeScore : holeScores) {
      if (holeScore.score == 0) {
        issues.add(new RoundIssue(holeScore.hole,
            String.format("Hole %d has a score of 0.", holeScore.hole.number)));
      }
      if (holeScore.putts >= holeScore.score) {
        issues.add(new RoundIssue(holeScore.hole,
            String.format("Hole %d has more putts than total score.", holeScore.hole.number)));
      }
    }

    return issues;
  }

  @NonNull
  public static Round create(@NonNull final User user, @NonNull final Course course,
                             @NonNull final HoleSet holeSet) {

    int randomId = 0 - (new SecureRandom().nextInt() + 1);
    return create(randomId, user, course, course.ratings[0], DateTime
            .now(),
        true, 0, false, 0, new HoleScore[0], holeSet);
  }

  @NonNull
  public static Round create(final long id, @NonNull final User user, @NonNull final Course course,
                             @NonNull final CourseRating rating, @NonNull final DateTime time,
                             final boolean official, final int handicap,
                             final boolean isHandicapOverridden,
                             final int handicapOverride, @NonNull final HoleScore[] oldHoleScores,
                             @NonNull final HoleSet holeSet) {

    Arrays.sort(course.holes);

    HoleScore[] newHoleScores = new HoleScore[holeSet.numHoles];

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
                rating.holeRating(holeNum).par > 3, true, new ArrayList<Shot>(),
                course.holes[holeNum - 1]);
      } else {
        newHoleScores[holeNum - holeSet.holeStart] = oldHoleScore;
      }
    }

    return new Round(id, user, course, rating, time, official, handicap, isHandicapOverridden,
        handicapOverride, newHoleScores).handicapped();
  }

  public static class RoundIssue {

    public final Hole hole;
    public final String message;

    public RoundIssue(@NonNull final Hole hole, @NonNull final String message) {
      this.hole = hole;
      this.message = message;
    }
  }
}
