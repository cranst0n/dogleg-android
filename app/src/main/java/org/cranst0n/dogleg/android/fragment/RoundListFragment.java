package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cranst0n.dogleg.android.R;

public class RoundListFragment extends Fragment {

  private View mViewFragmentRoundList;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    mViewFragmentRoundList = inflater.inflate(R.layout.fragment_round_list, container, false);

    return mViewFragmentRoundList;
  }
}
