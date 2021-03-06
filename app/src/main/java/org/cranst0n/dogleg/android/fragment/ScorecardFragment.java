package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;
import org.cranst0n.dogleg.android.views.TextlessCheckbox;

public class ScorecardFragment extends BaseFragment {

  @Nullable
  protected Round round;

  protected boolean enabled = true;

  protected View scorecardView;
  protected TableLayout scorecardTable;

  protected TableRow front9HoleNumberRow;
  protected TableRow front9ParRow;
  protected TableRow front9YardageRow;
  protected TableRow front9ScoreRow;
  protected TableRow front9NetScoreRow;
  protected TableRow front9PuttsRow;
  protected TableRow front9PenaltiesRow;
  protected TableRow front9FairwayHitRow;
  protected TableRow front9GirRow;

  protected TextView front9ParText;
  protected TextView front9YardageText;
  protected TextView front9ScoreText;
  protected TextView front9NetScoreText;
  protected TextView front9PuttsText;
  protected TextView front9PenaltiesText;
  protected TextView front9FairwayHitText;
  protected TextView front9GirText;

  protected TableRow back9HoleNumberRow;
  protected TableRow back9ParRow;
  protected TableRow back9YardageRow;
  protected TableRow back9ScoreRow;
  protected TableRow back9NetScoreRow;
  protected TableRow back9PuttsRow;
  protected TableRow back9PenaltiesRow;
  protected TableRow back9FairwayHitRow;
  protected TableRow back9GirRow;

  protected TextView back9ParText;
  protected TextView back9YardageText;
  protected TextView back9ScoreText;
  protected TextView back9NetScoreText;
  protected TextView back9PuttsText;
  protected TextView back9PenaltiesText;
  protected TextView back9FairwayHitText;
  protected TextView back9GirText;

  protected HoleViewHolder[] holeFieldViews;

  public static ScorecardFragment instance(@NonNull final Round round) {
    ScorecardFragment fragment = new ScorecardFragment();
    fragment.setRound(round);
    return fragment;
  }

  public ScorecardFragment() {

  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    scorecardView = inflater.inflate(R.layout.include_scorecard, container, false);

    findViews(scorecardView);
    updateScorecard();

    return scorecardView;
  }

  @Nullable
  public Round getRound() {
    return round;
  }

  public void setRound(@NonNull final Round round) {
    this.round = round;
    updateScorecard();
  }

  protected void updateScorecard() {
    if (round != null && isAdded()) {

      HoleSet holeSet = round.holeSet();

      for (int holeNum = holeStart(); holeNum <= holeEnd(); holeNum++) {

        if (holeSet.includes(holeNum)) {

          int ix = holeNum - holeStart();
          HoleRating holeRating = round.rating.holeRating(holeNum);
          HoleScore holeScore = round.holeScore(holeNum);

          if (holeRating != null) {
            holeFieldViews[ix].par.setText(String.valueOf(holeRating.par));
            holeFieldViews[ix].yardage.setText(String.valueOf(holeRating.yardage));
            holeFieldViews[ix].fairwayHit.setEnabled(enabled && holeRating.par > 3);
          }

          if (holeScore != null) {
            updateHole(holeScore);
          }
        }
      }

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

      updateRoundStats(round);
    }
  }

  public int holeStart() {
    return 1;
  }

  public int holeEnd() {
    return 18;
  }

  public int numHoles() {
    return holeEnd() - holeStart() + 1;
  }

  public boolean includesHole(final int holeNumber) {
    return holeNumber >= holeStart() && holeNumber <= holeEnd();
  }

  public void updateHole(@NonNull final HoleScore holeScore) {

    if (includesHole(holeScore.hole.number) && holeFieldViews != null) {

      int viewIdx = holeScore.hole.number - holeStart();

      HoleViewHolder viewHolder = holeFieldViews[viewIdx];
      viewHolder.score.setText(String.valueOf(holeScore.score));
      viewHolder.netScore.setText(String.valueOf(holeScore.netScore));
      viewHolder.putts.setText(String.valueOf(holeScore.putts));
      viewHolder.penalties.setText(String.valueOf(holeScore.penaltyStrokes));
      viewHolder.fairwayHit.setChecked(holeScore.fairwayHit);
      viewHolder.gir.setChecked(holeScore.gir);

      viewHolder.updateScoreIndicators();
    }

    if (round != null) {
      updateRoundStats(round);
    }
  }

  protected void updateRoundStats(@NonNull final Round round) {

    if (isAdded()) {
      RoundStats stats = round.stats();

      if (holeStart() == 1) {
        front9ParText.setText(String.valueOf(round.rating.frontPar()));
        front9YardageText.setText(String.valueOf(round.rating.frontYardage()));
        front9ScoreText.setText(String.valueOf(stats.frontScore));
        front9NetScoreText.setText(String.valueOf(stats.frontNetScore));
        front9PuttsText.setText(String.valueOf(stats.frontPutts));
        front9PenaltiesText.setText(String.valueOf(stats.frontPenalties));
        front9FairwayHitText.setText(formatPercentage(stats.frontFairwayHitPercentage));
        front9GirText.setText(formatPercentage(stats.frontGirPercentage));
      }

      if (holeEnd() == 18) {
        back9ParText.setText(String.valueOf(round.rating.backPar()));
        back9YardageText.setText(String.valueOf(round.rating.backYardage()));
        back9ScoreText.setText(String.valueOf(stats.backScore));
        back9NetScoreText.setText(String.valueOf(stats.backNetScore));
        back9PuttsText.setText(String.valueOf(stats.backPutts));
        back9PenaltiesText.setText(String.valueOf(stats.backPenalties));
        back9FairwayHitText.setText(formatPercentage(stats.backFairwayHitPercentage));
        back9GirText.setText(formatPercentage(stats.backGirPercentage));
      }
    }
  }

  @NonNull
  private String formatPercentage(final double percentage) {
    if (percentage >= 1) {
      return String.format("%d%%", Math.round(percentage * 100));
    } else if (percentage > 0) {
      return String.format("%.1f%%", percentage * 100);
    } else {
      return "0%";
    }
  }

  public void setFront9Visibility(final int visibility) {
    if (holeStart() == 1) {
      front9HoleNumberRow.setVisibility(visibility);
      front9ParRow.setVisibility(visibility);
      front9YardageRow.setVisibility(visibility);
      front9ScoreRow.setVisibility(visibility);
      front9NetScoreRow.setVisibility(visibility);
      front9PuttsRow.setVisibility(visibility);
      front9PenaltiesRow.setVisibility(visibility);
      front9FairwayHitRow.setVisibility(visibility);
      front9GirRow.setVisibility(visibility);

      if (holeEnd() == 18) {

        int back9TopMargin = (visibility == View.INVISIBLE || visibility == View.GONE) ? 0 : 8;

        ViewGroup.LayoutParams lp = back9HoleNumberRow.getLayoutParams();
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(lp);
        llp.setMargins(0, back9TopMargin, 0, 0);
        back9HoleNumberRow.setLayoutParams(llp);
      }
    }
  }

  public void setBack9Visibility(final int visibility) {
    if (holeEnd() == 18) {
      back9HoleNumberRow.setVisibility(visibility);
      back9ParRow.setVisibility(visibility);
      back9YardageRow.setVisibility(visibility);
      back9ScoreRow.setVisibility(visibility);
      back9NetScoreRow.setVisibility(visibility);
      back9PuttsRow.setVisibility(visibility);
      back9PenaltiesRow.setVisibility(visibility);
      back9FairwayHitRow.setVisibility(visibility);
      back9GirRow.setVisibility(visibility);
    }
  }

  public ScorecardFragment setEnabled(final boolean enabled) {

    this.enabled = enabled;

    if (holeFieldViews != null) {
      for (HoleViewHolder viewHolder : holeFieldViews) {
        viewHolder.fairwayHit.setEnabled(enabled);
        viewHolder.gir.setEnabled(enabled);
      }
    }

    return this;
  }

  protected void findViews(@NonNull final View parentView) {

    scorecardTable = (TableLayout) parentView.findViewById(R.id.scorecard_table);

    front9HoleNumberRow = (TableRow) scorecardTable.findViewById(R.id.front_9_hole_number_row);
    front9ParRow = (TableRow) scorecardTable.findViewById(R.id.front_9_par_row);
    front9YardageRow = (TableRow) scorecardTable.findViewById(R.id.front_9_yardage_row);
    front9ScoreRow = (TableRow) scorecardTable.findViewById(R.id.front_9_score_row);
    front9NetScoreRow = (TableRow) scorecardTable.findViewById(R.id.front_9_net_score_row);
    front9PuttsRow = (TableRow) scorecardTable.findViewById(R.id.front_9_putts_row);
    front9PenaltiesRow = (TableRow) scorecardTable.findViewById(R.id.front_9_penalties_row);
    front9FairwayHitRow = (TableRow) scorecardTable.findViewById(R.id.front_9_fairway_hit_row);
    front9GirRow = (TableRow) scorecardTable.findViewById(R.id.front_9_gir_row);

    back9HoleNumberRow = (TableRow) scorecardTable.findViewById(R.id.back_9_hole_number_row);
    back9ParRow = (TableRow) scorecardTable.findViewById(R.id.back_9_par_row);
    back9YardageRow = (TableRow) scorecardTable.findViewById(R.id.back_9_yardage_row);
    back9ScoreRow = (TableRow) scorecardTable.findViewById(R.id.back_9_score_row);
    back9NetScoreRow = (TableRow) scorecardTable.findViewById(R.id.back_9_net_score_row);
    back9PuttsRow = (TableRow) scorecardTable.findViewById(R.id.back_9_putts_row);
    back9PenaltiesRow = (TableRow) scorecardTable.findViewById(R.id.back_9_penalties_row);
    back9FairwayHitRow = (TableRow) scorecardTable.findViewById(R.id.back_9_fairway_hit_row);
    back9GirRow = (TableRow) scorecardTable.findViewById(R.id.back_9_gir_row);

    front9ParText = (TextView) scorecardTable.findViewById(R.id.front_9_par);
    front9YardageText = (TextView) scorecardTable.findViewById(R.id.front_9_yardage);
    front9ScoreText = (TextView) scorecardTable.findViewById(R.id.front_9_score);
    front9NetScoreText = (TextView) scorecardTable.findViewById(R.id.front_9_net_score);
    front9PuttsText = (TextView) scorecardTable.findViewById(R.id.front_9_putts);
    front9PenaltiesText = (TextView) scorecardTable.findViewById(R.id.front_9_penalties);
    front9FairwayHitText = (TextView) scorecardTable.findViewById(R.id.front_9_fairway_hit);
    front9GirText = (TextView) scorecardTable.findViewById(R.id.front_9_gir);

    back9ParText = (TextView) scorecardTable.findViewById(R.id.back_9_par);
    back9YardageText = (TextView) scorecardTable.findViewById(R.id.back_9_yardage);
    back9ScoreText = (TextView) scorecardTable.findViewById(R.id.back_9_score);
    back9NetScoreText = (TextView) scorecardTable.findViewById(R.id.back_9_net_score);
    back9PuttsText = (TextView) scorecardTable.findViewById(R.id.back_9_putts);
    back9PenaltiesText = (TextView) scorecardTable.findViewById(R.id.back_9_penalties);
    back9FairwayHitText = (TextView) scorecardTable.findViewById(R.id.back_9_fairway_hit);
    back9GirText = (TextView) scorecardTable.findViewById(R.id.back_9_gir);

    holeFieldViews = new HoleViewHolder[holeEnd() - holeStart() + 1];

    for (int ix = holeStart(); ix <= holeEnd(); ix++) {
      holeFieldViews[ix - holeStart()] = new HoleViewHolder(ix, parentView);
    }

    if (round != null) {
      setRound(round);
    }

    setEnabled(enabled);
  }

  protected class HoleViewHolder {

    public final int holeNumber;

    public final TextView number;
    public final TextView yardage;
    public final TextView par;
    public final TextView score;
    public final TextView netScore;
    public final TextView putts;
    public final TextView penalties;
    public final TextlessCheckbox fairwayHit;
    public final TextlessCheckbox gir;

    private HoleViewHolder(final int holeNumber, @NonNull final View parentView) {

      this.holeNumber = holeNumber;

      number = (TextView) holeView(holeNumber, "number", parentView);
      par = (TextView) holeView(holeNumber, "par", parentView);
      yardage = (TextView) holeView(holeNumber, "yardage", parentView);
      score = (TextView) holeView(holeNumber, "score", parentView);
      netScore = (TextView) holeView(holeNumber, "net_score", parentView);
      putts = (TextView) holeView(holeNumber, "putts", parentView);
      penalties = (TextView) holeView(holeNumber, "penalties", parentView);
      fairwayHit = (TextlessCheckbox) holeView(holeNumber, "fairway_hit", parentView);
      gir = (TextlessCheckbox) holeView(holeNumber, "gir", parentView);

      fairwayHit.setEnabled(enabled);
      gir.setEnabled(enabled);
    }

    public void updateScoreIndicators() {

      if (round != null) {

        HoleScore holeScore = round.holeScore(holeNumber);

        if (holeScore != null) {
          HoleRating holeRating = round.rating.holeRating(holeScore.hole.number);

          if (holeRating != null) {

            int scoreToPar = holeScore.score - holeRating.par;
            int netScoreToPar = holeScore.netScore - holeRating.par;

            if (holeScore.score > 0) {
              score.setBackgroundResource(scoreBackgroundResource(scoreToPar));
              score.setTextColor(getResources().getColor(scoreTextColor(scoreToPar)));
              netScore.setBackgroundResource(scoreBackgroundResource(netScoreToPar));
              netScore.setTextColor(getResources().getColor(scoreTextColor(netScoreToPar)));
            } else {
              score.setBackgroundResource(scoreBackgroundResource(0));
              score.setTextColor(getResources().getColor(scoreTextColor(0)));
              netScore.setBackgroundResource(scoreBackgroundResource(0));
              netScore.setTextColor(getResources().getColor(scoreTextColor(0)));
            }
          }
        }
      }
    }

    private View holeView(final int holeNum,
                          @NonNull final String fieldSuffix,
                          final View parentView) {

      String s = String.format("hole_%d_%s", holeNum, fieldSuffix);
      return parentView.findViewById(
          getResources().getIdentifier(s, "id", activity.getPackageName()));
    }

    @DrawableRes
    private int scoreBackgroundResource(final int scoreToPar) {
      if (scoreToPar <= -2) {
        return R.drawable.score_eagle;
      } else if (scoreToPar == -1) {
        return R.drawable.score_birdie;
      } else if (scoreToPar == 0) {
        return R.drawable.score_par;
      } else if (scoreToPar == 1) {
        return R.drawable.score_bogey;
      } else {
        return R.drawable.score_double_bogey;
      }
    }

    @ColorRes
    private int scoreTextColor(final int scoreToPar) {
      if (scoreToPar <= -2) {
        return R.color.text_grey;
      } else if (scoreToPar == -1) {
        return android.R.color.white;
      } else if (scoreToPar == 0) {
        return R.color.text_grey;
      } else if (scoreToPar == 1) {
        return android.R.color.white;
      } else {
        return android.R.color.white;
      }
    }
  }

}
