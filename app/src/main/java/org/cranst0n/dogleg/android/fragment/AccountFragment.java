package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.SnackBars;

public class AccountFragment extends BaseFragment {

  private Bus bus;
  private Users users;
  private User currentUser;

  private View accountView;

  private EditText oldPasswordField;
  private EditText newPasswordField;
  private EditText newPasswordConfirmField;
  private Button changePasswordButton;

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
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    accountView = inflater.inflate(R.layout.fragment_account, container, false);

    oldPasswordField = (EditText) accountView.findViewById(R.id.old_password_field);
    newPasswordField = (EditText) accountView.findViewById(R.id.new_password_field);
    newPasswordConfirmField = (EditText) accountView.findViewById(R.id.new_password_confirm_field);
    changePasswordButton = (Button) accountView.findViewById(R.id.change_password_button);

    changePasswordButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        users.changePassword(currentUser, oldPasswordField.getText().toString(), newPasswordField
            .getText().toString(), newPasswordConfirmField.getText().toString())
            .onSuccess(new BackendResponse.BackendSuccessListener<User>() {
              @Override
              public void onSuccess(final User value) {
                SnackBars.showSimple(activity, "Password changed.");
                oldPasswordField.setText("");
                newPasswordField.setText("");
                newPasswordConfirmField.setText("");
              }
            })
            .onError(SnackBars.showBackendError(activity, "Change Failed:"))
            .onException(SnackBars.showBackendException(activity, "Change Failed:"));
      }
    });

    return accountView;
  }

  @Subscribe
  public void newUser(final User user) {
    currentUser = user;

    if (changePasswordButton != null) {
      changePasswordButton.setEnabled(currentUser.isValid());
    }
  }
}
