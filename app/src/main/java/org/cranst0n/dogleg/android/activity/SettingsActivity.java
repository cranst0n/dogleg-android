package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;

public class SettingsActivity extends BaseActivity {

  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View v = super.onCreateView(inflater, container, savedInstanceState);

      if (v != null) {
        ListView lv = (ListView) v.findViewById(android.R.id.list);
        lv.setPadding(lv.getPaddingLeft(), 20,
            lv.getPaddingRight(), lv.getPaddingBottom());
      }

      return v;
    }
  }

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
