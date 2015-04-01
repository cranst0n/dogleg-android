package org.cranst0n.dogleg.android.model;

public class UserProfile {

  public final String home;
  public final LatLon location;
  public final Image avatar;
  public final Course favoriteCourse;

  public static UserProfile EMPTY = new UserProfile("", new LatLon(0, 0), Image.EMPTY, null);

  public UserProfile(final String home, final LatLon location, final Image avatar,
                     final Course favoriteCourse) {

    this.home = home;
    this.location = location;
    this.avatar = avatar;
    this.favoriteCourse = favoriteCourse;
  }

}
