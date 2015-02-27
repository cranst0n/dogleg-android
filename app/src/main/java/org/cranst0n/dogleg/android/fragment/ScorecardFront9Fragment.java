package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.Round;

public class ScorecardFront9Fragment extends ScorecardFragment {

  public static ScorecardFront9Fragment instance(final Round round) {
    return new ScorecardFront9Fragment(round);
  }

  protected ScorecardFront9Fragment(final Round round) {
    super(round);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    scorecardView = inflater.inflate(R.layout.include_scorecard_front_9, container, false);

    findViews();

    return scorecardView;
  }

  @Override
  public int holeStart() {
    return 1;
  }

  @Override
  public int holeEnd() {
    return 9;
  }
}
