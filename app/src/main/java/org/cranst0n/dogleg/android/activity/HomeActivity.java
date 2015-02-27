package org.cranst0n.dogleg.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.fragment.HomeFragment;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.SnackBarUtils;

public class HomeActivity extends BaseActivity {

  private final String Tag = getClass().getSimpleName();

  private Authentication authentication;
  private Bus bus;

  private User currentUser = DoglegApplication.appUser();

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    authentication = new Authentication(this);
    authentication.authUser();

    bus = BusProvider.instance();
    bus.register(this);

    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction().add(R.id.content_frame, new HomeFragment()).commit();
    }

    if (authentication.serverUrl().isEmpty()) {
      promptServerUrl();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bus.unregister(this);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.home_activity_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {

    menu.setGroupVisible(R.id.group_logged_out, currentUser.equals(User.NO_USER));
    menu.setGroupVisible(R.id.group_logged_in, !currentUser.equals(User.NO_USER));

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_login: {
        showLoginDialog();
        return true;
      }
      case R.id.action_logout: {
        authentication.logout().onSuccess(new BackendResponse.BackendSuccessListener<BackendMessage>() {
          @Override
          public void onSuccess(final BackendMessage value) {
            SnackBarUtils.showSimple(HomeActivity.this, value.message);
          }
        });
        return true;
      }
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected int setLayoutResourceIdentifier() {
    return R.layout.screen_default;
  }

  @Override
  protected int getTitleToolBar() {
    return R.string.app_name;
  }

  @Subscribe
  public void newUser(final User user) {

    currentUser = user;

    if (!currentUser.equals(User.NO_USER)) {
      SnackBarUtils.showSimple(HomeActivity.this, "Welcome " + currentUser.name + "!");
    } else {

    }

    invalidateOptionsMenu();
  }

  private void promptServerUrl() {

    SnackbarManager.show(
        Snackbar.with(HomeActivity.this)
            .text("No server URL set.")
            .actionLabel("Configure URL")
            .actionColor(getResources().getColor(R.color.warn))
            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            .swipeToDismiss(false)
            .actionListener(new ActionClickListener() {
              @Override
              public void onActionClicked(Snackbar snackbar) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
              }
            })
        , HomeActivity.this);
  }

  private EditText usernameInput;
  private EditText passwordInput;

  private void showLoginDialog() {

    MaterialDialog dialog = new MaterialDialog.Builder(this)
        .customView(R.layout.dialog_login, true)
        .positiveText(R.string.action_login)
        .negativeText(android.R.string.cancel)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog dialog) {
            doLogin(usernameInput.getText().toString(), passwordInput.getText().toString());
          }

          @Override
          public void onNegative(final MaterialDialog dialog) {

          }
        }).build();

    final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
    usernameInput = (EditText) dialog.getCustomView().findViewById(R.id.username);
    passwordInput = (EditText) dialog.getCustomView().findViewById(R.id.password);

    passwordInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
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

  private void doLogin(final String username, final String password) {

    authentication.login(username, password).
        onError(new BackendResponse.BackendErrorListener() {
          @Override
          public void onError(BackendMessage message) {
            SnackBarUtils.showSimple(HomeActivity.this, "Login failed: " + message.message);
          }
        }).
        onException(new BackendResponse.BackendExceptionListener() {
          @Override
          public void onException(final Exception exception) {
            SnackBarUtils.showSimple(HomeActivity.this, "Login failed: " + exception.getMessage());
          }
        });
  }

}
