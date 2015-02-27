package org.cranst0n.dogleg.android.fragment.api;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import org.cranst0n.dogleg.android.activity.api.BaseActivity;
import org.cranst0n.dogleg.android.fragment.TitledFragment;

public abstract class BaseFragment extends Fragment implements TitledFragment {

  protected Context context;
  protected Activity activity;

  public BaseFragment() {
    super();
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
    this.context = this.activity.getApplicationContext();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    activity = null;
    context = null;
  }

  @Override
  public String getTitle() {
    return getClass().getSimpleName();
  }

  protected void setActionBarTitle(final String title) {
    if (activity instanceof ActionBarActivity) {
      ((ActionBarActivity) activity).getSupportActionBar().setTitle(title);
    }
  }

  protected void setToolbarBackground(final int color) {
    if (activity instanceof BaseActivity) {
      ((BaseActivity) activity).setToolbarBackground(color);
    }
  }

  protected void setToolbarOverlaid(final boolean overlay) {
    if (activity instanceof BaseActivity) {
      ((BaseActivity) activity).setToolbarOverlaid(overlay);
    }
  }

}
