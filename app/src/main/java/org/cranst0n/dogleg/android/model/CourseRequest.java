package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class CourseRequest {

  @NonNull
  public final String name;
  @NonNull
  public final String city;
  @NonNull
  public final String state;
  @NonNull
  public final String country;
  @NonNull
  public final String website;
  @NonNull
  public final String comment;

  public CourseRequest(@NonNull final String name, @NonNull final String city,
                       @NonNull final String state, @NonNull final String country,
                       @NonNull final String website, @NonNull final String comment) {

    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.website = website;
    this.comment = comment;
  }
}
