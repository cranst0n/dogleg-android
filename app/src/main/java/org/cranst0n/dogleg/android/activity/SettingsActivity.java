package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;

public class SettingsActivity extends BaseActivity {

  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mNavigationDrawerFragment.setDrawerIndicatorEnabled(false);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
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
  protected int setLayoutResourceIdentifier() {
    return R.layout.screen_default;
  }

  @Override
  protected int getTitleToolBar() {
    return R.string.settings_activity_title;
  }
}
