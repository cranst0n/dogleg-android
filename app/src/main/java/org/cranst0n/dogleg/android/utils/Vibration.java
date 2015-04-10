package org.cranst0n.dogleg.android.utils;

import android.content.Context;
import android.os.Vibrator;

import org.cranst0n.dogleg.android.DoglegApplication;

public class Vibration {

  public static void vibrate() {
    vibrate(1000);
  }

  public static void vibrate(final long duration) {
    Vibrator v = (Vibrator) DoglegApplication.context().getSystemService(Context.VIBRATOR_SERVICE);
    v.vibrate(300);
  }
}
