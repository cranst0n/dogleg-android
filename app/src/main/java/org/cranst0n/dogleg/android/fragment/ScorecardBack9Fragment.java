package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.Round;

public class ScorecardBack9Fragment extends ScorecardFragment {

  public static ScorecardBack9Fragment instance(final Round round) {
    ScorecardBack9Fragment fragment = new ScorecardBack9Fragment();
    fragment.setRound(round);
    return fragment;
  }

  public ScorecardBack9Fragment() {
    super();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    scorecardView = inflater.inflate(R.layout.include_scorecard_back_9, container, false);

    findViews(scorecardView);

    return scorecardView;
  }

  @Override
  public int holeStart() {
    return 10;
  }

  @Override
  public int holeEnd() {
    return 18;
  }

}
