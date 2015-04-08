package org.cranst0n.dogleg.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.ShotCaddySetupFragment;

public class ShotCaddySetupActivity extends BaseActivity {

  private final String TAG = getClass().getSimpleName();

  private ShotCaddySetupFragment setupFragment;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    setupFragment = new ShotCaddySetupFragment();

    getSupportFragmentManager().beginTransaction().
        add(R.id.activity_base_content_frame, setupFragment).commit();
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onNewIntent(final Intent intent) {
    setupFragment.onNewIntent(intent);
  }

  @Override
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getTitleToolBar() {
    return R.string.shot_caddy_setup_activity_title;
  }

}
