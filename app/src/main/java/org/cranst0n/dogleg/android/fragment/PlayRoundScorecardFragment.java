package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.PlayRoundActivity;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;

public class PlayRoundScorecardFragment extends BaseFragment {

  private PlayRoundFragment.PlayRoundListener playRoundListener;

  private View scorecardView;
  private TableLayout scorecardTable;

  private TableRow front9HoleNumberRow;
  private TableRow front9ParRow;
  private TableRow front9YardageRow;
  private TableRow front9ScoreRow;
  private TableRow front9PuttsRow;
  private TableRow front9PenaltiesRow;
  private TableRow front9FairwayHitRow;
  private TableRow front9GirRow;

  private TextView front9ParText;
  private TextView front9YardageText;
  private TextView front9ScoreText;
  private TextView front9PuttsText;
  private TextView front9PenaltiesText;
  private TextView front9FairwayHitText;
  private TextView front9GirText;

  private TableRow back9HoleNumberRow;
  private TableRow back9ParRow;
  private TableRow back9YardageRow;
  private TableRow back9ScoreRow;
  private TableRow back9PuttsRow;
  private TableRow back9PenaltiesRow;
  private TableRow back9FairwayHitRow;
  private TableRow back9GirRow;

  private TextView back9ParText;
  private TextView back9YardageText;
  private TextView back9ScoreText;
  private TextView back9PuttsText;
  private TextView back9PenaltiesText;
  private TextView back9FairwayHitText;
  private TextView back9GirText;

  private final HoleViewHolder[] holeFieldViews = new HoleViewHolder[18];

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


    scorecardView = inflater.inflate(R.layout.fragment_play_round_scorecard, container, false);
    scorecardTable = (TableLayout) scorecardView.findViewById(R.id.scorecard_table);

    front9HoleNumberRow = (TableRow) scorecardTable.findViewById(R.id.front_9_hole_number_row);
    front9ParRow = (TableRow) scorecardTable.findViewById(R.id.front_9_par_row);
    front9YardageRow = (TableRow) scorecardTable.findViewById(R.id.front_9_yardage_row);
    front9ScoreRow = (TableRow) scorecardTable.findViewById(R.id.front_9_score_row);
    front9PuttsRow = (TableRow) scorecardTable.findViewById(R.id.front_9_putts_row);
    front9PenaltiesRow = (TableRow) scorecardTable.findViewById(R.id.front_9_penalties_row);
    front9FairwayHitRow = (TableRow) scorecardTable.findViewById(R.id.front_9_fairway_hit_row);
    front9GirRow = (TableRow) scorecardTable.findViewById(R.id.front_9_gir_row);

    back9HoleNumberRow = (TableRow) scorecardTable.findViewById(R.id.back_9_hole_number_row);
    back9ParRow = (TableRow) scorecardTable.findViewById(R.id.back_9_par_row);
    back9YardageRow = (TableRow) scorecardTable.findViewById(R.id.back_9_yardage_row);
    back9ScoreRow = (TableRow) scorecardTable.findViewById(R.id.back_9_score_row);
    back9PuttsRow = (TableRow) scorecardTable.findViewById(R.id.back_9_putts_row);
    back9PenaltiesRow = (TableRow) scorecardTable.findViewById(R.id.back_9_penalties_row);
    back9FairwayHitRow = (TableRow) scorecardTable.findViewById(R.id.back_9_fairway_hit_row);
    back9GirRow = (TableRow) scorecardTable.findViewById(R.id.back_9_gir_row);

    front9ParText = (TextView) scorecardTable.findViewById(R.id.front_9_par);
    front9YardageText = (TextView) scorecardTable.findViewById(R.id.front_9_yardage);
    front9ScoreText = (TextView) scorecardTable.findViewById(R.id.front_9_score);
    front9PuttsText = (TextView) scorecardTable.findViewById(R.id.front_9_putts);
    front9PenaltiesText = (TextView) scorecardTable.findViewById(R.id.front_9_penalties);
    front9FairwayHitText = (TextView) scorecardTable.findViewById(R.id.front_9_fairway_hit);
    front9GirText = (TextView) scorecardTable.findViewById(R.id.front_9_gir);

    back9ParText = (TextView) scorecardTable.findViewById(R.id.back_9_par);
    back9YardageText = (TextView) scorecardTable.findViewById(R.id.back_9_yardage);
    back9ScoreText = (TextView) scorecardTable.findViewById(R.id.back_9_score);
    back9PuttsText = (TextView) scorecardTable.findViewById(R.id.back_9_putts);
    back9PenaltiesText = (TextView) scorecardTable.findViewById(R.id.back_9_penalties);
    back9FairwayHitText = (TextView) scorecardTable.findViewById(R.id.back_9_fairway_hit);
    back9GirText = (TextView) scorecardTable.findViewById(R.id.back_9_gir);

    for (int ix = 0; ix < holeFieldViews.length; ix++) {
      holeFieldViews[ix] = new HoleViewHolder(ix + 1);
    }

    updateScorecard();

    return scorecardView;
  }

  private Round round() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).round();
    }

    return null;
  }

  public void roundUpdated(final Round round) {
    updateScorecard();
  }

  public void holeScoreUpdated(final HoleScore holeScore) {
    if (isAdded()) {
      int viewIdx = holeScore.hole.number - 1;
      HoleViewHolder viewHolder = holeFieldViews[viewIdx];
      viewHolder.score.setText(String.valueOf(holeScore.score));
      viewHolder.putts.setText(String.valueOf(holeScore.putts));
      viewHolder.penalties.setText(String.valueOf(holeScore.penaltyStrokes));
      viewHolder.fairwayHit.setChecked(holeScore.fairwayHit);
      viewHolder.gir.setChecked(holeScore.gir);

      updateRoundStats(round());
    }
  }

  private void updateScorecard() {

    Round round = round();

    if (round != null) {

      HoleSet holeSet = round.holeSet();

      for (int holeNum = 0; holeNum < holeFieldViews.length; holeNum++) {
        if (holeSet.includes(holeNum + 1)) {
          holeFieldViews[holeNum].par.setText(String.valueOf(round.rating.holeRatings[holeNum].par));
          holeFieldViews[holeNum].yardage.setText(String.valueOf(round.rating.holeRatings[holeNum].yardage));
          holeFieldViews[holeNum].fairwayHit.setEnabled(round.rating.holeRatings[holeNum].par > 3);
          holeScoreUpdated(round.holeScores[holeNum - holeSet.holeStart + 1]);
        }

        front9ParText.setText(String.valueOf(round.rating.frontPar()));
        front9YardageText.setText(String.valueOf(round.rating.frontYardage()));
        back9ParText.setText(String.valueOf(round.rating.backPar()));
        back9YardageText.setText(String.valueOf(round.rating.backYardage()));

        if (holeSet == HoleSet.Front9) {
          setFront9Visibility(View.VISIBLE);
          setBack9Visibility(View.GONE);
        } else if (holeSet == HoleSet.Back9) {
          setFront9Visibility(View.GONE);
          setBack9Visibility(View.VISIBLE);
        } else {
          setFront9Visibility(View.VISIBLE);
          setBack9Visibility(View.VISIBLE);
        }
      }

      updateRoundStats(round);
    }
  }

  private void updateRoundStats(final Round round) {

    RoundStats stats = round.stats();

    front9ScoreText.setText(String.valueOf(stats.frontScore));
    front9PuttsText.setText(String.valueOf(stats.frontPutts));
    front9PenaltiesText.setText(String.valueOf(stats.frontPenalties));
    back9ScoreText.setText(String.valueOf(stats.backScore));
    back9PuttsText.setText(String.valueOf(stats.backPutts));
    back9PenaltiesText.setText(String.valueOf(stats.backPenalties));

    front9FairwayHitText.setText(String.format("%.2f%%", stats.frontFairwayHitPercentage * 100));
    front9GirText.setText(String.format("%.2f%%", stats.frontGirPercentage * 100));
    back9FairwayHitText.setText(String.format("%.2f%%", stats.backFairwayHitPercentage * 100));
    back9GirText.setText(String.format("%.2f%%", stats.backGirPercentage * 100));
  }

  private void setFront9Visibility(final int visibility) {
    front9HoleNumberRow.setVisibility(visibility);
    front9ParRow.setVisibility(visibility);
    front9YardageRow.setVisibility(visibility);
    front9ScoreRow.setVisibility(visibility);
    front9PuttsRow.setVisibility(visibility);
    front9PenaltiesRow.setVisibility(visibility);
    front9FairwayHitRow.setVisibility(visibility);
    front9GirRow.setVisibility(visibility);
  }

  private void setBack9Visibility(final int visibility) {
    back9HoleNumberRow.setVisibility(visibility);
    back9ParRow.setVisibility(visibility);
    back9YardageRow.setVisibility(visibility);
    back9ScoreRow.setVisibility(visibility);
    back9PuttsRow.setVisibility(visibility);
    back9PenaltiesRow.setVisibility(visibility);
    back9FairwayHitRow.setVisibility(visibility);
    back9GirRow.setVisibility(visibility);
  }

  private class HoleViewHolder {

    public final TextView number;
    public final TextView yardage;
    public final TextView par;
    public final TextView score;
    public final TextView putts;
    public final TextView penalties;
    public final CheckBox fairwayHit;
    public final CheckBox gir;
    public final View divider;

    private HoleViewHolder(final int holeNumber) {

      number = (TextView) holeView(holeNumber, "number");
      par = (TextView) holeView(holeNumber, "par");
      yardage = (TextView) holeView(holeNumber, "yardage");
      score = (TextView) holeView(holeNumber, "score");
      putts = (TextView) holeView(holeNumber, "putts");
      penalties = (TextView) holeView(holeNumber, "penalties");
      fairwayHit = (CheckBox) holeView(holeNumber, "fairway_hit");
      gir = (CheckBox) holeView(holeNumber, "gir");
      divider = holeView(holeNumber, "divider");

      score.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          if (playRoundListener != null) {
            playRoundListener.showScoreSelectionDialog(holeNumber);
          }
        }
      });

      putts.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (playRoundListener != null) {
            playRoundListener.showPuttsSelectionDialog(holeNumber);
          }
        }
      });

      penalties.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (playRoundListener != null) {
            playRoundListener.showPenaltiesSelectionDialog(holeNumber);
          }
        }
      });

      fairwayHit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            playRoundListener.updateScore(round().holeScores[holeNumber - 1].fairwayHit(isChecked));
          }
        }
      });

      gir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
          if (playRoundListener != null) {
            playRoundListener.updateScore(round().holeScores[holeNumber - 1].gir(isChecked));
          }
        }
      });
    }

    private View holeView(final int holeNum, final String fieldSuffix) {
      String s = String.format("hole_%d_%s", holeNum, fieldSuffix);
      return scorecardView.findViewById(
          getResources().getIdentifier(s, "id", activity.getPackageName()));
    }
  }

}
