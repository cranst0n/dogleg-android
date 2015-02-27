package org.cranst0n.dogleg.android.model;

public enum HoleSet {

  All("All 18", 1, 18), Front9("Front 9", 1, 9), Back9("Back 9", 10, 18);

  private HoleSet(final String title, final int holeStart, final int holeEnd) {
    this.title = title;
    this.holeStart = holeStart;
    this.holeEnd = holeEnd;
    this.numHoles = holeEnd - holeStart + 1;
  }

  public boolean includes(final int holeNumber) {
    return holeNumber >= holeStart && holeNumber <= holeEnd;
  }

  public final String title;
  public final int holeStart;
  public final int holeEnd;
  public final int numHoles;

  public static HoleSet[] available(final Course course) {
    if (course.numHoles == 18) {
      return HoleSet.values();
    } else {
      return new HoleSet[]{Front9};
    }
  }
}
