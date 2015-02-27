package org.cranst0n.dogleg.android.fragment.api;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

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

}
