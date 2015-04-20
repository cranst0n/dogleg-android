package org.cranst0n.dogleg.android.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.cranst0n.dogleg.android.model.LatLon;

public class Intents {

  public static final int PICK_IMAGE = 1;

  private Intents() {

  }

  public static Intent navigationTo(@NonNull final LatLon location) {
    return new Intent(android.content.Intent.ACTION_VIEW,
        Uri.parse(String.format(
            "http://maps.google.com/maps?daddr=%.5f,%.5f", location.latitude, location.longitude)));
  }

  public static Intent phoneCall(@NonNull final String phoneNumber) {
    return new Intent(Intent.ACTION_DIAL, Uri.parse(String.format("tel:%s", phoneNumber)));
  }

  public static Intent pickImage() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType("image/*");
    return intent;
  }
}
