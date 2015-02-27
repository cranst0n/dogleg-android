package org.cranst0n.dogleg.android.model;

public class User {

  public final long id;
  public final String name;
  public final String email;
  public final boolean admin;
  public final boolean active;

  public static final User NO_USER = new User(-1, "", "", false, false);

  public User(final long id, final String name, final String email,
              final boolean admin, final boolean active) {

    this.id = id;
    this.name = name;
    this.email = email;
    this.admin = admin;
    this.active = active;
  }
}
