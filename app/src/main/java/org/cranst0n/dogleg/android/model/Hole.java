package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Hole implements Comparable<Hole> {

  public final long id;
  public final int number;
  public final long courseId;
  @NonNull
  public final HoleFeature[] features;

  public Hole(final long id, final int number, final long courseId,
              @NonNull final HoleFeature[] features) {

    this.id = id;
    this.number = number;
    this.courseId = courseId;
    this.features = features;
  }

  public boolean hasFlybyPath() {
    return findByName("flyby") != null;
  }

  @Nullable
  public HoleFeature flybyPathFeature() {
    return findByName("flyby");
  }

  @Nullable
  public HoleFeature teeFeature() {
    return findByName("tee");
  }

  @Nullable
  public HoleFeature greenFeature() {
    return findByName("green");
  }

  @NonNull
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
  public int compareTo(@Nullable final Hole another) {
    return another == null ? -1 : number - another.number;
  }

  @Nullable
  private HoleFeature findByName(@NonNull final String name) {
    for (HoleFeature feature : features) {
      if (feature.name.toLowerCase().equals(name)) {
        return feature;
      }
    }

    return null;
  }

  @NonNull
  public static Hole empty() {
    return new Hole(-1, -1, -1, new HoleFeature[0]);
  }
}
