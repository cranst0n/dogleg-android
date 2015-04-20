package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class CourseSummary {

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
  public final int par;
  @NonNull
  public final Exclusivity exclusivity;
  @NonNull
  public final String phoneNumber;
  @NonNull
  public final LatLon location;
  public final long creatorId;
  public final boolean approved;

  public CourseSummary(final long id, @NonNull final String name, @NonNull final String city,
                       @NonNull final String state, @NonNull final String country,
                       final int numHoles, final int par, @NonNull final Exclusivity exclusivity,
                       @NonNull final String phoneNumber, @NonNull final LatLon location,
                       final long creatorId, final boolean approved) {

    this.id = id;
    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.numHoles = numHoles;
    this.par = par;
    this.exclusivity = exclusivity;
    this.phoneNumber = phoneNumber;
    this.location = location;
    this.creatorId = creatorId;
    this.approved = approved;
  }

}
