package org.cranst0n.dogleg.android.model;

public enum HoleSet {

  All("All 18", 1, 18), Front9("Front 9", 1, 9), Back9("Back 9", 10, 18);

  private HoleSet(final String title, final int holeStart, final int holeEnd) {
    this.title = title;
    this.holeStart = holeStart;
    this.holeEnd = holeEnd;
  }

  public final String title;
  public final int holeStart;
  public final int holeEnd;

}
