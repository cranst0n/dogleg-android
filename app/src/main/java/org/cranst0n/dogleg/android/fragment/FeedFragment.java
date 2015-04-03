package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cranst0n.dogleg.android.R;

public class FeedFragment extends BaseFragment {

  private View feedView;

  public static FeedFragment newInstance() {
    return new FeedFragment();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    feedView = inflater.inflate(R.layout.fragment_feed, container, false);
    return feedView;
  }

  @Override
  public String getTitle() {
    return "Feed";
  }
}
