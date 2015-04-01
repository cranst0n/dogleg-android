package org.cranst0n.dogleg.android.model;

public class Image {

  public final long id;
  public final byte[] data;

  public static Image EMPTY = new Image(-1, new byte[0]);

  public Image(final long id, final byte[] data) {
    this.id = id;
    this.data = data;
  }
}
