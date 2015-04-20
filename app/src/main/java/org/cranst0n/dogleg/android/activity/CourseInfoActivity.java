package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.CourseInfoFragment;

public class CourseInfoActivity extends BaseActivity {

  private CourseInfoFragment courseInfoFragment;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    if (savedInstanceState == null) {
      courseInfoFragment = new CourseInfoFragment();
      getSupportFragmentManager().beginTransaction().
          add(R.id.activity_base_content_frame, courseInfoFragment).commit();
    } else {
      courseInfoFragment = (CourseInfoFragment) getSupportFragmentManager().
          getFragment(savedInstanceState, CourseInfoFragment.class.getCanonicalName());
    }
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);

    getSupportFragmentManager().putFragment(
        outState, CourseInfoFragment.class.getCanonicalName(), courseInfoFragment);
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
