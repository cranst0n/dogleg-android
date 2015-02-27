package org.cranst0n.dogleg.android.model;

public class HoleRating {

  public final int number;
  public final int par;
  public final int yardage;
  public final int handicap;

  public HoleRating(final int number, final int par, final int yardage, final int handicap) {
    this.number = number;
    this.par = par;
    this.yardage = yardage;
    this.handicap = handicap;
  }
}
