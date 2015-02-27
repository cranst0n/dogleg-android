package org.cranst0n.dogleg.android.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.utils.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Course {

  private static String Tag = Course.class.getSimpleName();

  public final long id;
  public final String name;
  public final String city;
  public final String state;
  public final String country;
  public final int numHoles;
  public final LatLon location;
  public final Hole[] holes;
  public final CourseRating[] ratings;
  public final long creatorId;
  public final boolean approved;

  public Course(final long id, final String name, final String city, final String state,
                       final String country, final int numHoles, final LatLon location,
                       final Hole[] holes, final CourseRating[] ratings,
                       final long creatorId, final boolean approved) {

    this.id = id;
    this.name = name;
    this.city = city;
    this.state = state;
    this.country = country;
    this.numHoles = numHoles;
    this.location = location;
    this.holes = holes;
    this.ratings = ratings;
    this.creatorId = creatorId;
    this.approved = approved;
  }

  public CourseSummary summary() {
    return new CourseSummary(id, name, city, state, country, numHoles, location, creatorId,
        approved);
  }

}
