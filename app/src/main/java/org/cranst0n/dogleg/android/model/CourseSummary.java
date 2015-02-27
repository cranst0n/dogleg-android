package org.cranst0n.dogleg.android.model;

public class CourseSummary {

  public final long id;
  public final String name;
  public final String city;
  public final String state;
  public final String country;
  public final int numHoles;
  public final LatLon location;
  public final long creatorId;
  public final boolean approved;

  public CourseSummary(final long id, final String name, final String city, final String state,
                       final String country, final int numHoles, final LatLon location,
                       final long creatorId, final boolean approved) {

    this.id = id;
    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.numHoles = numHoles;
    this.location = location;
    this.creatorId = creatorId;
    this.approved = approved;
  }

}
