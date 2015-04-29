package org.cranst0n.dogleg.android.activity;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.DrawerFragment;

public abstract class BaseActivity extends ActionBarActivity {

  protected final DoglegApplication app = DoglegApplication.application();

  protected Toolbar toolbar;
  protected DrawerLayout drawerLayout;
  protected DrawerFragment drawerFragment;

  @LayoutRes
  protected abstract int getLayoutResourceIdentifier();

  @StringRes
  protected abstract int getToolbarTitle();

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    setContentView(getLayoutResourceIdentifier());

    drawerLayout = (DrawerLayout) findViewById(R.id.activity_base_drawer_layout);
    drawerFragment = (DrawerFragment)
        getSupportFragmentManager().findFragmentById(R.id.activity_base_drawer);

    drawerFragment.setUp(
        R.id.activity_base_drawer,
        (DrawerLayout) findViewById(R.id.activity_base_drawer_layout));

    toolbar = (Toolbar) findViewById(R.id.screen_default_toolbar);
    toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(getToolbarTitle());
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setElevation(0);

    final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
    getSupportActionBar().setHomeAsUpIndicator(upArrow);
  }

  protected void onResume() {
    super.onResume();
    app.setCurrentActivity(this);
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
    Activity currActivity = app.currentActivity();
    if (currActivity != null && currActivity.equals(this)) {
      app.setCurrentActivity(null);
    }
  }

  public void setToolbarBackground(@ColorRes final int color) {
    toolbar.setBackgroundColor(color);
  }

  public void setToolbarOverlaid(final boolean overlay) {

    int marginTop = overlay ? 0 : actionBarHeight();

    FrameLayout fl = (FrameLayout) findViewById(R.id.activity_base_content_frame);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fl.getLayoutParams();
    params.setMargins(0, marginTop, 0, 0);
    fl.setLayoutParams(params);
  }

  private int actionBarHeight() {
    final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
        new int[]{android.R.attr.actionBarSize});

    int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
    styledAttributes.recycle();

    return actionBarHeight;
  }
}
