package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.view.View;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.ProfileFragment;
import org.cranst0n.dogleg.android.utils.Colors;

public class ProfileActivity extends BaseActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setToolbarOverlaid(true);
    setToolbarBackground(Colors.setAlpha(0, R.color.primary));

    toolbar.setVisibility(View.GONE);

    drawerFragment.setDrawerIndicatorEnabled(false);

    getSupportFragmentManager().beginTransaction().
        add(R.id.activity_base_content_frame, new ProfileFragment()).commit();
  }

  @Override
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getToolbarTitle() {
    return R.string.app_name;
  }
}
