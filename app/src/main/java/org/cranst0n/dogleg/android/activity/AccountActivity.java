package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;
import org.cranst0n.dogleg.android.fragment.AccountFragment;

public class AccountActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    drawerFragment.setDrawerIndicatorEnabled(false);

    getSupportFragmentManager().beginTransaction().
        add(R.id.activity_base_content_frame, new AccountFragment()).commit();
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
    return R.string.account_activity_title;
  }

}
