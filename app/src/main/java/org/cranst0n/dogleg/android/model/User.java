package org.cranst0n.dogleg.android.model;

import org.joda.time.DateTime;

public class User {

  public final long id;
  public final String name;
  public final String password;
  public final String email;
  public final boolean admin;
  public final boolean active;
  public final DateTime created;
  public final UserProfile profile;

  public static final User NO_USER = new User(-1, "", "", "", false, false,
      DateTime.now(), UserProfile.EMPTY);

  public User(final long id, final String name, final String password, final String email,
              final boolean admin, final boolean active, final DateTime created,
              final UserProfile profile) {

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
    if (!(obj instanceof User)) {
      return false;
    }
    if (obj == this) {
      return true;
    }

    return ((User) obj).id == id;
  }

  @Override
  public int hashCode() {
    return 31 + new Long(id).hashCode();
  }
}
