package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.cranst0n.dogleg.android.activity.PlayRoundActivity;
import org.cranst0n.dogleg.android.model.Round;

public class PlayRoundScorecardFragment extends ScorecardFragment {

  private PlayRoundFragment.PlayRoundListener playRoundListener;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    if (activity instanceof PlayRoundFragment.PlayRoundListener) {
      playRoundListener = (PlayRoundFragment.PlayRoundListener) activity;
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    super.onCreateView(inflater, container, savedInstanceState);

    for (int holeIx = 0; holeIx < numHoles(); holeIx++) {

      final int holeNumber = holeStart() + holeIx;

      holeFieldViews[holeNumber - holeStart()].score.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          if (playRoundListener != null) {
            playRoundListener.showScoreSelectionDialog(holeNumber);
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].putts.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (playRoundListener != null) {
            playRoundListener.showPuttsSelectionDialog(holeNumber);
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].penalties.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (playRoundListener != null) {
            playRoundListener.showPenaltiesSelectionDialog(holeNumber);
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].fairwayHit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            playRoundListener.updateScore(round().holeScore(holeNumber).fairwayHit(isChecked));
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].gir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            playRoundListener.updateScore(round().holeScore(holeNumber).gir(isChecked));
          }
        }
      });
    }

    return scorecardView;
  }

  private Round round() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).round();
    }

    return null;
  }

}
