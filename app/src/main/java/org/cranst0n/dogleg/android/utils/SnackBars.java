package org.cranst0n.dogleg.android.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;

public class SnackBars {

  private SnackBars() {

  }

  public static void showSimple(@Nullable final Activity activity,
                                @NonNull final String message) {

    if (activity != null) {
      SnackbarManager.show(
          Snackbar.with(activity)
              .text(message)
              .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
          , activity);
    }
  }

  public static void showSimpleMultiline(@Nullable final Activity activity,
                                         @NonNull final String message) {

    if (activity != null) {
      SnackbarManager.show(
          Snackbar.with(activity)
              .type(SnackbarType.MULTI_LINE)
              .text(message)
              .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
          , activity);
    }
  }

  public static BackendResponse.BackendErrorListener showBackendError(@Nullable final Activity
                                                                          activity) {

    return showBackendError(activity, "");
  }

  public static BackendResponse.BackendErrorListener showBackendError(@Nullable final
                                                                      Activity activity,
                                                                      @NonNull final String
                                                                          prefix) {

    return new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(@NonNull final BackendMessage message) {

        String text;

        if (!prefix.isEmpty() && message.message != null) {
          text = String.format("%s %s", prefix, message.message);
        } else if (message.message != null) {
          text = message.message;
        } else {
          text = prefix;
        }

        showSimple(activity, text);
      }
    };
  }

  public static BackendResponse.BackendExceptionListener showBackendException(
      @Nullable final Activity activity) {
    return showBackendException(activity, "");
  }

  public static BackendResponse.BackendExceptionListener showBackendException(
      @Nullable final Activity activity, @NonNull final String prefix) {

    return new BackendResponse.BackendExceptionListener() {
      @Override
      public void onException(@NonNull final Exception exception) {

        String text = prefix.isEmpty() ? exception.getMessage() : String.format("%s %s", prefix,
            exception.getMessage());

        showSimple(activity, text);
      }
    };
  }
}
