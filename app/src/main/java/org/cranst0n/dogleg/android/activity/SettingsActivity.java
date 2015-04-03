package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction().replace(R.id.activity_base_content_frame, new SettingsFragment()).commit();
    }
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
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getTitleToolBar() {
    return R.string.settings_activity_title;
  }
}
