package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public enum HoleSet {

  All("All 18", 1, 18), Front9("Front 9", 1, 9), Back9("Back 9", 10, 18);

  HoleSet(@NonNull final String title, final int holeStart, final int holeEnd) {
    this.title = title;
    this.holeStart = holeStart;
    this.holeEnd = holeEnd;
    this.numHoles = holeEnd - holeStart + 1;
  }

  public boolean includes(final int holeNumber) {
    return holeNumber >= holeStart && holeNumber <= holeEnd;
  }

  @NonNull
  public final String title;
  public final int holeStart;
  public final int holeEnd;
  public final int numHoles;

  @NonNull
  public static HoleSet[] available(@NonNull final Course course) {
    if (course.numHoles == 18) {
      return HoleSet.values();
    } else {
      return new HoleSet[]{Front9};
    }
  }
}
