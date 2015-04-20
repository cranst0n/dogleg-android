package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class AuthToken {

  @NonNull
  public final String token;

  public AuthToken(@NonNull final String token) {
    this.token = token;
  }
}
