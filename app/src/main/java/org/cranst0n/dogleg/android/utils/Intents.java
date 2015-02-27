package org.cranst0n.dogleg.android.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.cranst0n.dogleg.android.model.LatLon;

public class Intents {

  private Intents() {

  }

  public static final Intent navigationTo(final LatLon location) {
    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
        Uri.parse(String.format(
            "http://maps.google.com/maps?daddr=%.5f,%.5f", location.latitude, location.longitude)));
    return intent;
  }
}
