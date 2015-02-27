package org.cranst0n.dogleg.android.activity.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.HomeActivity;
import org.cranst0n.dogleg.android.activity.SettingsActivity;
import org.cranst0n.dogleg.android.constants.DrawerMenu;
import org.cranst0n.dogleg.android.fragment.NavigationDrawerFragment;

public abstract class BaseActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

  protected NavigationDrawerFragment mNavigationDrawerFragment;

  protected abstract int setLayoutResourceIdentifier();

  protected abstract int getTitleToolBar();

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(setLayoutResourceIdentifier());

    mNavigationDrawerFragment = (NavigationDrawerFragment)
        getSupportFragmentManager().findFragmentById(R.id.screen_default_navigation_drawer);

    mNavigationDrawerFragment.setUp(
        R.id.screen_default_navigation_drawer,
        (DrawerLayout) findViewById(R.id.screen_default_drawer_layout));

    getSupportActionBar().setTitle(getTitleToolBar());
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setElevation(0);
  }

  protected void onResume() {
    super.onResume();
    DoglegApplication.application().setCurrentActivity(this);
  }

  protected void onPause() {
    clearReferences();
    super.onPause();
  }

  protected void onDestroy() {
    clearReferences();
    super.onDestroy();
  }

  private void clearReferences() {
    Activity currActivity = DoglegApplication.application().currentActivity();
    if (currActivity != null && currActivity.equals(this)) {
      DoglegApplication.application().setCurrentActivity(null);
    }
  }

  @Override
  public void onNavigationDrawerItemSelected(final int position) {

    switch (position) {
      case DrawerMenu.HOME: {
        if (getClass() != HomeActivity.class) {
          Intent intent = new Intent(this, HomeActivity.class);
          intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          startActivity(intent);
        } else {
          mNavigationDrawerFragment.closeDrawer();
        }
        break;
      }
      case DrawerMenu.SETTINGS:
        if (getClass() != SettingsActivity.class) {
          Intent intent = new Intent(this, SettingsActivity.class);
          intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
          startActivity(intent);
        } else {
          mNavigationDrawerFragment.closeDrawer();
        }

        break;
    }
  }

  private void fragmentTransaction(final Fragment fragment) {
    if (fragment != null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.content_frame, fragment)
          .commit();
    }
  }
}
