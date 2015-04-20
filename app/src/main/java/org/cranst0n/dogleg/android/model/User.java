package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

public class User {

  public final long id;
  @NonNull
  public final String name;
  @NonNull
  public final String password;
  @NonNull
  public final String email;
  public final boolean admin;
  public final boolean active;
  @NonNull
  public final DateTime created;
  @NonNull
  public final UserProfile profile;

  public static final User NO_USER = new User(-1, "", "", "", false, false,
      DateTime.now(), UserProfile.EMPTY);

  public User(final long id, @NonNull final String name, @NonNull final String password,
              @NonNull final String email, final boolean admin, final boolean active,
              @NonNull final DateTime created, @NonNull final UserProfile profile) {

    this.id = id;
    this.name = name;
    this.password = password;
    this.email = email;
    this.admin = admin;
    this.active = active;
    this.created = created;
    this.profile = profile;
  }

  public boolean isValid() {
    return !this.equals(NO_USER);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof User && ((User) obj).id == id;
  }

  @Override
  public int hashCode() {
    return 31 + Long.valueOf(id).hashCode();
  }
}
