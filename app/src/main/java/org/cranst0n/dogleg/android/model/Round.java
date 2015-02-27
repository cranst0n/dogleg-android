package org.cranst0n.dogleg.android.model;

public class Round {

  private long id = -1;
  private User user;
  private Course course;
  private CourseRating rating;
  private long time;
  private boolean official;
  private int handicap = 0;
  private boolean isHandicapOverridden = false;
  private int handicapOverride;

  public Round(final User user, final Course course, final CourseRating rating, final long time,
               final boolean official, final int handicap, final boolean isHandicapOverridden,
               final int handicapOverride) {

    this.user = user;
    this.course = course;
    this.rating = rating;
    this.time = time;
    this.official = official;
    this.handicap = handicap;
    this.isHandicapOverridden = isHandicapOverridden;
    this.handicapOverride = handicapOverride;
  }

}
