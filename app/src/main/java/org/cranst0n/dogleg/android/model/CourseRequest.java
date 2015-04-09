package org.cranst0n.dogleg.android.model;

public class CourseRequest {

  public final String name;
  public final String city;
  public final String state;
  public final String country;
  public final String website;
  public final String comment;

  public CourseRequest(final String name, final String city, final String state,
                       final String country, final String website, final String comment) {

    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.website = website;
    this.comment = comment;
  }
}
