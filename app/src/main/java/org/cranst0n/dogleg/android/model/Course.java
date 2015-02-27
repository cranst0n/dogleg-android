package org.cranst0n.dogleg.android.model;

public class Course {

  private static String Tag = Course.class.getSimpleName();

  public final long id;
  public final String name;
  public final String city;
  public final String state;
  public final String country;
  public final int numHoles;
  public final Exclusivity exclusivity;
  public final String phoneNumber;
  public final LatLon location;
  public final Hole[] holes;
  public final CourseRating[] ratings;
  public final long creatorId;
  public final boolean approved;

  public Course(final long id, final String name, final String city, final String state,
                final String country, final int numHoles, final Exclusivity exclusivity,
                final String phoneNumber, final LatLon location, final Hole[] holes,
                final CourseRating[] ratings, final long creatorId, final boolean approved) {

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

  public CourseSummary summary() {
    return new CourseSummary(id, name, city, state, country, numHoles, par(), exclusivity,
        phoneNumber, location, creatorId, approved);
  }

}
