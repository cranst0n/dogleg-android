package org.cranst0n.dogleg.android.utils;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Files {

  @NonNull
  public static String getStringFromFile(@NonNull final File f) throws IOException {

    FileInputStream fin = new FileInputStream(f);
    String ret = convertStreamToString(fin);

    fin.close();
    return ret;
  }

  @NonNull
  private static String convertStreamToString(@NonNull final InputStream is) throws IOException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;

    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }

    reader.close();

    return sb.toString();
  }
}
