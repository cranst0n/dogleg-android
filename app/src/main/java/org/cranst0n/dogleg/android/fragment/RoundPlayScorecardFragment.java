package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.RoundPlayActivity;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.views.TextlessCheckbox;

public class RoundPlayScorecardFragment extends ScorecardFragment {

  private RoundPlayFragment.PlayRoundListener playRoundListener;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    if (activity instanceof RoundPlayFragment.PlayRoundListener) {
      playRoundListener = (RoundPlayFragment.PlayRoundListener) activity;
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    View roundScorecardView =
        inflater.inflate(R.layout.fragment_round_play_scorecard, container, false);
    findViews(roundScorecardView);

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

      holeFieldViews[holeNumber - holeStart()].fairwayHit.setOnCheckedChangeListener(new TextlessCheckbox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(@NonNull final TextlessCheckbox buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            Round round = round();
            if (round != null) {
              HoleScore holeScore = round.holeScore(holeNumber);
              if (holeScore != null) {
                playRoundListener.updateScore(holeScore.fairwayHit(isChecked));
              }
            }
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].gir.setOnCheckedChangeListener(new TextlessCheckbox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(@NonNull final TextlessCheckbox buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            Round round = round();
            if (round != null) {
              HoleScore holeScore = round.holeScore(holeNumber);
              if (holeScore != null) {
                playRoundListener.updateScore(holeScore.gir(isChecked));
              }
            }
          }
        }
      });
    }

    return roundScorecardView;
  }

  @Nullable
  private Round round() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).round();
    }

    return null;
  }

}
