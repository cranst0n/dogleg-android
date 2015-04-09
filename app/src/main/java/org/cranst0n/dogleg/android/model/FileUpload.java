package org.cranst0n.dogleg.android.model;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class FileUpload {

  public final long filesize;
  public final String filetype;
  public final String filename;
  public final String content;

  public FileUpload(final long filesize, final String filetype, final String filename,
                    final byte[] content) {
    this(filesize, filetype, filename, Base64.encodeToString(content, Base64.NO_WRAP));
  }

  public FileUpload(final long filesize, final String filetype, final String filename,
                    final String content) {

    this.filesize = filesize;
    this.filetype = filetype;
    this.filename = filename;
    this.content = content;
  }

  public FileUpload(final Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    byte[] byteArray = stream.toByteArray();

    this.filesize = byteArray.length;
    this.filetype = "jpeg";
    this.filename = "bitmap.jpeg";
    this.content = Base64.encodeToString(byteArray, Base64.NO_WRAP);
  }

}
