package org.cranst0n.dogleg.android.utils;

import android.app.Activity;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

public class SnackBars {

  public static final void showSimple(final Activity activity, final String message) {
    if (activity != null) {
      SnackbarManager.show(
          Snackbar.with(activity)
              .text(message)
              .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
          , activity);
    }
  }

  public static final void showSimpleMultiline(final Activity activity, final String message) {
    if (activity != null) {
      SnackbarManager.show(
          Snackbar.with(activity)
              .type(SnackbarType.MULTI_LINE)
              .text(message)
              .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
          , activity);
    }
  }

}
