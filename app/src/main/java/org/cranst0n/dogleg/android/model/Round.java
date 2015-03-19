package org.cranst0n.dogleg.android.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Round {

  public final long id = -1;
  public final User user;
  public final Course course;
  public final CourseRating rating;
  public final long time;
  public final boolean official;
  public final int handicap;
  public final boolean isHandicapOverridden;
  public final int handicapOverride;

  private final HoleScore[] holeScores;

  private HoleSet holeSet;

  private Round(final User user, final Course course, final CourseRating rating, final long time,
                final boolean official, final int handicap, final boolean isHandicapOverridden,
                final int handicapOverride, final HoleScore[] holeScores) {

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

  public HoleScore[] holeScores() {
    return Arrays.copyOf(holeScores, holeScores.length);
  }

  public HoleScore holeScore(final int holeNumber) {
    for(HoleScore holeScore : holeScores) {
      if(holeScore.hole.number == holeNumber) {
        return holeScore;
      }
    }

    return null;
  }

  public void updateScore(final HoleScore holeScore) {
    int ix = holeScore.hole.number - holeSet().holeStart;
    holeScores[ix] = holeScore;
  }

  public Round asUser(final User user) {
    return new Round(user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  public Round withTime(final long time) {
    return new Round(user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
  }

  public Round withHandicap(final int handicap) {
    return new Round(user, course, rating, time, official,
        handicap, isHandicapOverridden, handicapOverride, holeScores);
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

  public static Round create(final User user, final Course course, final HoleSet holeSet) {
    return create(user, course, course.ratings[0], System.currentTimeMillis(),
        true, 0, false, 0, null, holeSet);
  }

  public static Round create(final User user, final Course course, final CourseRating rating,
                             final long time, final boolean official, final int handicap,
                             final boolean isHandicapOverridden, final int handicapOverride,
                             final HoleScore[] oldHoleScores, final HoleSet holeSet) {

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

    return new Round(user, course, rating, time, official, handicap, isHandicapOverridden,
        handicapOverride, newHoleScores);
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
