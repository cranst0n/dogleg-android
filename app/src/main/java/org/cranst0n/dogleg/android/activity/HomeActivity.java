package org.cranst0n.dogleg.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.koushikdutta.ion.Ion;
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
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.SnackBars;

public class HomeActivity extends BaseActivity {

  private final String Tag = getClass().getSimpleName();

  private Authentication authentication;
  private Bus bus;

  private User currentUser = DoglegApplication.appUser();

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    authentication = new Authentication(this);
    authentication.authUser();

    bus = BusProvider.Instance.bus;
    bus.register(this);

    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction().add(R.id.activity_base_content_frame, new HomeFragment()).commit();
    }

    if (authentication.serverUrl().isEmpty()) {
      warnEmptyServerUrl();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bus.unregister(this);

    Ion.getDefault(this).cancelAll(this);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.login_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {

    menu.setGroupVisible(R.id.group_logged_out, !currentUser.isValid());
    menu.setGroupVisible(R.id.group_logged_in, currentUser.isValid());

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_login: {
        Dialogs.showLoginDialog(this);
        return true;
      }
      case R.id.action_logout: {
        authentication.logout().onSuccess(new BackendResponse.BackendSuccessListener<BackendMessage>() {
          @Override
          public void onSuccess(final BackendMessage value) {
            SnackBars.showSimple(HomeActivity.this, value.message);
          }
        });
        return true;
      }
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getTitleToolBar() {
    return R.string.app_name;
  }

  @Subscribe
  public void newUser(final User user) {

    currentUser = user;

    if (currentUser.isValid()) {
      SnackBars.showSimple(HomeActivity.this, "Welcome " + currentUser.name + "!");
    }

    invalidateOptionsMenu();
  }

  private void warnEmptyServerUrl() {

    SnackbarManager.show(
        Snackbar.with(HomeActivity.this)
            .text("No server URL set.")
            .actionLabel("Configure URL")
            .actionColor(getResources().getColor(R.color.warn))
            .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
            .swipeToDismiss(false)
            .actionListener(new ActionClickListener() {
              @Override
              public void onActionClicked(final Snackbar snackbar) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
              }
            })
        , HomeActivity.this);
  }

}
