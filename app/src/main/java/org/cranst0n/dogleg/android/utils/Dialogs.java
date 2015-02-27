package org.cranst0n.dogleg.android.utils;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;

public class Dialogs {

  private static EditText usernameInput;
  private static EditText passwordInput;

  private Dialogs() {

  }

  public static void showLoginDialog(final Activity activity) {

    MaterialDialog dialog = new MaterialDialog.Builder(activity)
        .customView(R.layout.dialog_login, true)
        .positiveText(R.string.action_login)
        .negativeText(android.R.string.cancel)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog dialog) {
            doLogin(activity, usernameInput.getText().toString(), passwordInput.getText().toString());
          }
        }).build();

    final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
    usernameInput = (EditText) dialog.getCustomView().findViewById(R.id.username);
    passwordInput = (EditText) dialog.getCustomView().findViewById(R.id.password);

    passwordInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(final CharSequence s, final int start,
                                    final int count, final int after) {

      }

      @Override
      public void onTextChanged(final CharSequence s, final int start,
                                final int before, final int count) {
        positiveAction.setEnabled(usernameInput.getText().toString().trim().length() > 0 &&
            s.toString().trim().length() > 0);
      }

      @Override
      public void afterTextChanged(final Editable s) {

      }
    });

    dialog.show();
    positiveAction.setEnabled(false); // disabled by default
  }

  private static void doLogin(final Activity activity, final String username, final String password) {

    new Authentication(activity).login(username, password).
        onError(new BackendResponse.BackendErrorListener() {
          @Override
          public void onError(BackendMessage message) {
            SnackBars.showSimple(activity, "Login failed: " + message.message);
          }
        }).
        onException(new BackendResponse.BackendExceptionListener() {
          @Override
          public void onException(final Exception exception) {
            SnackBars.showSimple(activity, "Login failed: " + exception.getMessage());
          }
        });
  }
}
