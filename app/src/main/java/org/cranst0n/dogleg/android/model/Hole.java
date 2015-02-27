package org.cranst0n.dogleg.android.model;

import java.util.ArrayList;
import java.util.List;

public class Hole implements Comparable<Hole> {

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

  public boolean hasFlybyPath() {
    return findByName("flyby") != null;
  }

  public HoleFeature flybyPathFeature() {
    return findByName("flyby");
  }

  public HoleFeature teeFeature() {
    return findByName("tee");
  }

  public HoleFeature greenFeature() {
    return findByName("green");
  }

  public List<HoleFeature> displayableFeatures() {
    List<HoleFeature> featureList = new ArrayList<>();

    for (HoleFeature feature : features) {
      if (!feature.name.toLowerCase().equals("tee") &&
          !feature.name.toLowerCase().equals("flyby")) {

        featureList.add(feature);
      }
    }

    return featureList;
  }

  @Override
  public int compareTo(final Hole another) {
    return number - another.number;
  }

  private HoleFeature findByName(final String name) {
    for (HoleFeature feature : features) {
      if (feature.name.toLowerCase().equals(name)) {
        return feature;
      }
    }

    return null;
  }

  public static Hole empty() {
    return new Hole(-1, -1, -1, new HoleFeature[0]);
  }
}
