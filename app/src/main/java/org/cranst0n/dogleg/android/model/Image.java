package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

public class Image {

  public final long id;
  @NonNull
  public final byte[] data;

  public static final Image EMPTY = new Image(-1, new byte[0]);

  public Image(final long id, @NonNull final byte[] data) {
    this.id = id;
    this.data = data;
  }
}
