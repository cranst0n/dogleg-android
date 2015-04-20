package org.cranst0n.dogleg.android.model;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class FileUpload {

  public final long filesize;
  @NonNull
  public final String filetype;
  @NonNull
  public final String filename;
  @NonNull
  public final String content;

  public FileUpload(final long filesize, @NonNull final String filetype,
                    @NonNull final String filename, @NonNull final byte[] content) {
    this(filesize, filetype, filename, Base64.encodeToString(content, Base64.NO_WRAP));
  }

  public FileUpload(final long filesize, @NonNull final String filetype,
                    @NonNull final String filename, @NonNull final String content) {

    this.filesize = filesize;
    this.filetype = filetype;
    this.filename = filename;
    this.content = content;
  }

  public FileUpload(@NonNull final Bitmap bitmap) {

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    byte[] byteArray = stream.toByteArray();

    this.filesize = byteArray.length;
    this.filetype = "jpeg";
    this.filename = "bitmap.jpeg";
    this.content = Base64.encodeToString(byteArray, Base64.NO_WRAP);
  }

}
