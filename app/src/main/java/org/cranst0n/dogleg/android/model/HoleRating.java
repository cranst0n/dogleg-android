package org.cranst0n.dogleg.android.model;

public class HoleRating implements Comparable<HoleRating> {

  public final long id;
  public final int number;
  public final int par;
  public final int yardage;
  public final int handicap;

  public HoleRating(final long id, final int number, final int par, final int yardage, final int handicap) {
    this.id = id;
    this.number = number;
    this.par = par;
    this.yardage = yardage;
    this.handicap = handicap;
  }

  @Override
  public int compareTo(final HoleRating another) {
    return number - another.number;
  }

  public static HoleRating empty() {
    return new HoleRating(-1, -1, -1, -1, -1);
  }
}
