package org.cranst0n.dogleg.android.model;

public class Hole {

  public final long id;
  public final int number;
  public final long courseId;
  public final HoleFeature[] features;

  public Hole(final long id, final int number, final long courseId, final HoleFeature[] features) {
    this.id = id;
    this.number = number;
    this.courseId = courseId;
    this.features = features;
  }

}
