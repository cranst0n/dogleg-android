package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.CourseRequestFragment;

public class CourseRequestActivity extends BaseActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    getSupportFragmentManager().beginTransaction().
        add(R.id.activity_base_content_frame, new CourseRequestFragment()).commit();
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
  protected int getToolbarTitle() {
    return R.string.course_request_activity_title;
  }
}
