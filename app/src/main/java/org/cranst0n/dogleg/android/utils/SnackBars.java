package org.cranst0n.dogleg.android.utils;

import android.app.Activity;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;

public class SnackBars {

  private SnackBars() {

  }

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

  public static final BackendResponse.BackendErrorListener showBackendError(final Activity
                                                                                activity) {
    return showBackendError(activity, "");
  }

  public static final BackendResponse.BackendErrorListener showBackendError(final Activity activity,
                                                                            final String prefix) {

    return new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(final BackendMessage message) {

        String text = prefix.isEmpty() ? message.message : String.format("%s %s", prefix, message
            .message);

        showSimple(activity, text);
      }
    };
  }

  public static final BackendResponse.BackendExceptionListener showBackendException(
      final Activity activity) {
    return showBackendException(activity, "");
  }

  public static final BackendResponse.BackendExceptionListener showBackendException(
      final Activity activity, final String prefix) {

    return new BackendResponse.BackendExceptionListener() {
      @Override
      public void onException(final Exception exception) {

        String text = prefix.isEmpty() ? exception.getMessage() : String.format("%s %s", prefix,
            exception.getMessage());

        showSimple(activity, text);
      }
    };
  }
}
