package org.cranst0n.dogleg.android.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.model.UserProfile;
import org.joda.time.DateTime;

import java.util.List;

public class Dialogs {

  private static EditText usernameInput;
  private static EditText passwordInput;

  private Dialogs() {

  }

  public static MaterialDialog showBusyDialog(@NonNull final Activity activity,
                                              @NonNull final String text) {

    return new MaterialDialog.Builder(activity)
        .content(text)
        .progress(true, 0)
        .cancelable(false)
        .show();
  }

  public static MaterialDialog showMessageDialog(@NonNull final Activity activity,
                                                 @NonNull final String text) {

    return new MaterialDialog.Builder(activity)
        .content(text)
        .show();
  }

  public static void showLoginDialog(@NonNull final Activity activity) {

    MaterialDialog dialog = new MaterialDialog.Builder(activity)
        .customView(R.layout.dialog_login, true)
        .positiveText(R.string.action_login)
        .negativeText(android.R.string.cancel)
        .neutralText("Sign Up")
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog dialog) {
            doLogin(activity,
                usernameInput.getText().toString(), passwordInput.getText().toString());
          }

          @Override
          public void onNeutral(final MaterialDialog dialog) {
            dialog.dismiss();
            showSignupDialog(activity);
          }
        }).build();

    final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
    final View customView = dialog.getCustomView();

    if (customView != null) {

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
    }

    dialog.show();
    positiveAction.setEnabled(false); // disabled by default
  }

  public static void showSignupDialog(final Activity activity) {

    final MaterialDialog dialog = new MaterialDialog.Builder(activity)
        .customView(R.layout.include_sign_up_form, true)
        .positiveText("Sign Up")
        .negativeText(android.R.string.cancel)
        .autoDismiss(false)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog signupDialog) {

            View customView = signupDialog.getCustomView();

            if (customView != null) {

              final TextView usernameField =
                  (TextView) customView.findViewById(R.id.username_field);
              final TextView passwordField =
                  (TextView) customView.findViewById(R.id.password_field);
              final TextView passwordConfirmField =
                  (TextView) customView.findViewById(R.id.password_confirm_field);
              final TextView emailField =
                  (TextView) customView.findViewById(R.id.email_field);

              final String username = usernameField.getText().toString();
              String password = passwordField.getText().toString();
              String passwordConfirm = passwordConfirmField.getText().toString();
              final String email = emailField.getText().toString();

              if (username.isEmpty()) {
                usernameField.setError("Invalid username.");
                return;
              }

              if (!password.equals(passwordConfirm)) {
                passwordConfirmField.setError("Passwords do not match.");
                return;
              }

              List<String> passwordIssues = Strings.isPasswordStrong(password);
              if (!passwordIssues.isEmpty()) {
                passwordField.setError(passwordIssues.iterator().next());
              }

              if (!Strings.isEmailValid(email)) {
                emailField.setError("Invalid e-mail.");
                return;
              }

              String hashedPassword = Crypto.hashPassword(password);

              if (hashedPassword != null) {
                User newUser = new User(-1, username, hashedPassword, email, false, true,
                    DateTime.now(), UserProfile.EMPTY);

                final MaterialDialog busyDialog = showBusyDialog(activity, "Creating Account...");

                new Users(activity).create(newUser)
                    .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
                      @Override
                      public void onSuccess(@NonNull final User value) {
                        signupDialog.dismiss();
                        SnackBars.showSimple(activity, String.format("User '%s' created. Now login.",
                            username));
                      }
                    })
                    .onError(SnackBars.showBackendError(activity))
                    .onException(SnackBars.showBackendException(activity))
                    .onFinally(new BackendResponse.BackendFinallyListener() {
                      @Override
                      public void onFinally() {
                        busyDialog.dismiss();
                      }
                    });

              } else {
                SnackBars.showSimple(activity, "Error logging in: hash failed.");
              }
            }
          }

          @Override
          public void onNegative(final MaterialDialog dialog) {
            dialog.dismiss();
          }
        }).build();

    View customView = dialog.getCustomView();

    if (customView != null) {

      TextView administratorLabel = (TextView) dialog.getCustomView().findViewById(R.id
          .administrator_label);
      CheckBox administratorBox =
          (CheckBox) dialog.getCustomView().findViewById(R.id.administrator_box);
      Button createAccountButton = (Button) dialog.getCustomView().findViewById(R.id
          .create_account_button);

      administratorLabel.setVisibility(View.GONE);
      administratorBox.setVisibility(View.GONE);
      administratorBox.setChecked(false);
      createAccountButton.setVisibility(View.GONE);
      createAccountButton.setEnabled(false);
    }

    dialog.show();
  }

  private static void doLogin(final Activity activity, final String username,
                              final String password) {

    final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Logging in...");

    new Authentication(activity).login(username, password).
        onError(SnackBars.showBackendError(activity, "Login failed:")).
        onException(SnackBars.showBackendException(activity, "Login failed:")).
        onFinally(new BackendResponse.BackendFinallyListener() {
          @Override
          public void onFinally() {
            busyDialog.dismiss();
          }
        });
  }
}
