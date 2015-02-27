package org.cranst0n.dogleg.android.model;

import android.util.Log;

public class CourseRating {

  public final long id;
  public final String teeName;
  public final double rating;
  public final double slope;
  public final double frontRating;
  public final double frontSlope;
  public final double backRating;
  public final double backSlope;
  public final double bogeyRating;
  public final Gender gender;
  public final HoleRating[] holeRatings;

  private int par;
  private int yardage;

  public CourseRating(final long id, final String teeName, final double rating, final double slope,
                      final double frontRating, final double frontSlope, final double backRating,
                      final double backSlope, final double bogeyRating, final Gender gender,
                      final HoleRating[] holeRatings) {

    this.id = id;
    this.teeName = teeName;
    this.rating = rating;
    this.slope = slope;
    this.frontRating = frontRating;
    this.frontSlope = frontSlope;
    this.backRating = backRating;
    this.backSlope = backSlope;
    this.bogeyRating = bogeyRating;
    this.gender = gender;
    this.holeRatings = holeRatings;
  }

  public int par() {
    if(par == 0) {
      int sum = 0;
      for (int hole = 0; hole < holeRatings.length; hole++) {
        sum += holeRatings[hole].par;
      }
      par = sum;
    }
    return par;
  }

  public int yardage() {
    if(yardage == 0) {
      int sum = 0;
      for (int hole = 0; hole < holeRatings.length; hole++) {
        sum += holeRatings[hole].yardage;
      }
      yardage = sum;
    }
    return yardage;
  }

  @Override
  public String toString() {
    return teeName;
  }
}
