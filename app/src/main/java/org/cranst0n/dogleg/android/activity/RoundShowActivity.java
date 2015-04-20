package org.cranst0n.dogleg.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.CourseInfoFragment;
import org.cranst0n.dogleg.android.fragment.RoundShowFragment;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.utils.Json;

public class RoundShowActivity extends BaseActivity {

  private RoundShowFragment roundShowFragment;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    drawerFragment.setDrawerIndicatorEnabled(false);

    if (savedInstanceState == null) {
      roundShowFragment = new RoundShowFragment();
      getSupportFragmentManager().beginTransaction().
          add(R.id.activity_base_content_frame, roundShowFragment).commit();
    } else {
      roundShowFragment = (RoundShowFragment) getSupportFragmentManager().
          getFragment(savedInstanceState, CourseInfoFragment.class.getCanonicalName());
    }
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    getSupportFragmentManager().putFragment(
        outState, CourseInfoFragment.class.getCanonicalName(), roundShowFragment);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home: {
        backPressed();
        return true;
      }
      default: {
        return super.onOptionsItemSelected(item);
      }
    }
  }

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    //Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      backPressed();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getToolbarTitle() {
    return R.string.app_name;
  }

  private void backPressed() {
    Intent data = new Intent();
    data.putExtra(Round.class.getCanonicalName(),
        Json.pimpedGson().toJson(roundShowFragment.getRound()));

    setResult(R.integer.round_edit_result, data);

    finish();
  }
}
