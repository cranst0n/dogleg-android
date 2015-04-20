package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.adapter.UsernameSearchAdapter;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.model.UserProfile;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Crypto;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.utils.Strings;
import org.joda.time.DateTime;

import java.util.List;

public class AdminFragment extends BaseFragment {

  private Bus bus;
  private Users users;

  private TextView usernameField;
  private TextView passwordField;
  private TextView passwordConfirmField;
  private TextView emailField;
  private CheckBox administratorBox;

  private User resetUser = User.NO_USER;
  private AutoCompleteTextView usernameSearchField;
  private TextView resetPasswordField;
  private TextView resetPasswordConfirmField;
  private Button resetPasswordButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    users = new Users(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    View adminView = inflater.inflate(R.layout.fragment_admin, container, false);

    usernameField = (TextView) adminView.findViewById(R.id.username_field);
    passwordField = (TextView) adminView.findViewById(R.id.password_field);
    passwordConfirmField = (TextView) adminView.findViewById(R.id.password_confirm_field);
    emailField = (TextView) adminView.findViewById(R.id.email_field);
    administratorBox = (CheckBox) adminView.findViewById(R.id.administrator_box);
    Button createAccountButton = (Button) adminView.findViewById(R.id.create_account_button);

    createAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        createAccount();
      }
    });

    usernameSearchField = (AutoCompleteTextView) adminView.findViewById(R.id.username_search_field);
    usernameSearchField.setAdapter(new UsernameSearchAdapter(context));
    resetPasswordField = (TextView) adminView.findViewById(R.id.reset_password_field);
    resetPasswordConfirmField = (TextView) adminView.findViewById(R.id.reset_password_confirm_field);
    resetPasswordButton = (Button) adminView.findViewById(R.id.reset_password_button);
    resetPasswordButton.setEnabled(false);

    usernameSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        resetUser = (User) usernameSearchField.getAdapter().getItem(i);
        resetPasswordButton.setEnabled(true);
      }
    });

    resetPasswordButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        resetPassword();
      }
    });

    return adminView;
  }

  private void createAccount() {

    final String username = usernameField.getText().toString();
    String password = passwordField.getText().toString();
    String passwordConfirm = passwordConfirmField.getText().toString();
    final String email = emailField.getText().toString();
    boolean isAdmin = administratorBox.isChecked();

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

      User newUser = new User(-1, username, hashedPassword, email, isAdmin, true,
          DateTime.now(), UserProfile.EMPTY);

      final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Creating Account...");

      users.create(newUser)
          .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
            @Override
            public void onSuccess(@NonNull final User value) {
              SnackBars.showSimple(activity, String.format("User '%s' created.", username));

              usernameField.setText("");
              passwordField.setText("");
              passwordConfirmField.setText("");
              emailField.setText("");
              administratorBox.setChecked(false);
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
      SnackBars.showSimple(activity, "Failed to hash password.");
    }
  }

  private void resetPassword() {

    String password = resetPasswordField.getText().toString();
    String passwordConfirm = resetPasswordConfirmField.getText().toString();

    if (!password.equals(passwordConfirm)) {
      resetPasswordField.setError("Passwords do not match.");
      return;
    }

    List<String> passwordIssues = Strings.isPasswordStrong(password);
    if (!passwordIssues.isEmpty()) {
      resetPasswordField.setError(passwordIssues.iterator().next());
    }

    if (resetUser.isValid()) {

      final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Resetting Password...");

      users.resetPassword(resetUser, password, passwordConfirm)
          .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
            @Override
            public void onSuccess(@NonNull final User value) {
              SnackBars.showSimple(activity, "Password reset.");
              usernameSearchField.clearListSelection();
              resetPasswordField.setText("");
              resetPasswordConfirmField.setText("");
              resetPasswordButton.setEnabled(false);
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
      SnackBars.showSimple(activity, "Must select valid user to reset the password for.");
    }
  }

  @Subscribe
  public void newUser(final User user) {
    if (!user.isValid() || !user.admin) {
      activity.finish();
    }
  }
}
