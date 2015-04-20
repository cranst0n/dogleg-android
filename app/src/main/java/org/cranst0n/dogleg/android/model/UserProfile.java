package org.cranst0n.dogleg.android.model;

import android.support.annotation.Nullable;

public class UserProfile {

  @Nullable
  public final String home;
  @Nullable
  public final LatLon location;
  @Nullable
  public final Image avatar;
  @Nullable
  public final Course favoriteCourse;

  public static final UserProfile EMPTY = new UserProfile("", new LatLon(0, 0), Image.EMPTY, null);

  public UserProfile(@Nullable final String home, @Nullable final LatLon location,
                     @Nullable final Image avatar, @Nullable final Course favoriteCourse) {

    this.home = home;
    this.location = location;
    this.avatar = avatar;
    this.favoriteCourse = favoriteCourse;
  }

}
