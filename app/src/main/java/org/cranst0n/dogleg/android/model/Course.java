package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class Course {

  public final long id;
  @NonNull
  public final String name;
  @NonNull
  public final String city;
  @NonNull
  public final String state;
  @NonNull
  public final String country;
  public final int numHoles;
  @NonNull
  public final Exclusivity exclusivity;
  @NonNull
  public final String phoneNumber;
  @NonNull
  public final LatLon location;
  @NonNull
  public final Hole[] holes;
  @NonNull
  public final CourseRating[] ratings;
  public final long creatorId;
  public final boolean approved;

  public Course(final long id, @NonNull final String name, @NonNull final String city,
                @NonNull final String state, @NonNull final String country,
                final int numHoles, @NonNull final Exclusivity exclusivity,
                @NonNull final String phoneNumber, @NonNull final LatLon location,
                @NonNull final Hole[] holes, @NonNull final CourseRating[] ratings,
                final long creatorId, final boolean approved) {

    this.id = id;
    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.numHoles = numHoles;
    this.exclusivity = exclusivity;
    this.phoneNumber = phoneNumber;
    this.location = location;
    this.holes = holes;
    this.ratings = ratings;
    this.creatorId = creatorId;
    this.approved = approved;
  }

  public int par() {
    if (ratings.length > 0) {
      return ratings[0].par();
    } else {
      return 0;
    }
  }

  @NonNull
  public CourseSummary summary() {
    return new CourseSummary(id, name, city, state, country, numHoles, par(), exclusivity,
        phoneNumber, location, creatorId, approved);
  }

}
