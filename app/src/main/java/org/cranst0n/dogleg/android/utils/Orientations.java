package org.cranst0n.dogleg.android.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.NonNull;

public class Orientations {

  private Orientations() {

  }

  public static boolean isLocked(@NonNull final Activity activity) {
    int requestedOrientation = activity.getRequestedOrientation();

    return requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
        requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
  }

  public static void lockOrientation(@NonNull final Activity activity) {
    switch (activity.getResources().getConfiguration().orientation) {
      case Configuration.ORIENTATION_LANDSCAPE: {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        break;
      }
      default: {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      }
    }
  }

  public static void unlockOrientation(@NonNull Activity activity) {
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
  }

}
