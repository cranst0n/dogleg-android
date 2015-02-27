package org.cranst0n.dogleg.android.model;

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
  private int frontPar;
  private int frontYardage;
  private int backPar;
  private int backYardage;

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
    if (par == 0) {
      par = parFor(0, 18);
    }
    return par;
  }

  public int yardage() {
    if (yardage == 0) {
      yardage = yardageFor(0, 18);
    }
    return yardage;
  }

  public int frontPar() {
    if (frontPar == 0) {
      frontPar = parFor(0, 9);
    }
    return frontPar;
  }

  public int frontYardage() {
    if (frontYardage == 0) {
      frontYardage = yardageFor(0, 9);
    }
    return frontYardage;
  }

  public int backPar() {
    if (backPar == 0) {
      backPar = parFor(9, 18);
    }
    return backPar;
  }

  public int backYardage() {
    if (backYardage == 0) {
      backYardage = yardageFor(9, 18);
    }
    return backYardage;
  }

  private int parFor(final int holeStart, final int holeEnd) {
    int sum = 0;
    for (int hole = holeStart; hole < holeEnd && hole < holeRatings.length; hole++) {
      sum += holeRatings[hole].par;
    }

    return sum;
  }

  private int yardageFor(final int holeStart, final int holeEnd) {
    int sum = 0;
    for (int hole = holeStart; hole < holeEnd && hole < holeRatings.length; hole++) {
      sum += holeRatings[hole].yardage;
    }

    return sum;
  }

  @Override
  public String toString() {
    return teeName;
  }
}
