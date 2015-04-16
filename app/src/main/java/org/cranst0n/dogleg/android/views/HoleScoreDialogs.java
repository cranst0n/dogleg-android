package org.cranst0n.dogleg.android.views;

import android.app.Activity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;

public class HoleScoreDialogs {

  public static interface HoleScoreDialogCallback {
    void holeScoreUpdated(final HoleScore holeScore);
  }

  public static void showScoreSelectionDialog(final Activity activity, final Round round,
                                              final int holeNumber,
                                              final HoleScoreDialogCallback callback) {

    HoleRating holeRating = round.rating.holeRating(holeNumber);

    final int scoreStart = Math.max(holeRating.par - 3, 1);
    final int scoreEnd = scoreStart + 10;
    String[] scoreSelections = new String[scoreEnd - scoreStart + 1];
    for (int score = scoreStart; score <= scoreEnd; score++) {
      scoreSelections[score - scoreStart] =
          String.format("%d - (%s)", score, HoleScore.scoreToParString(score, holeRating.par));
    }

    new MaterialDialog.Builder(activity)
        .title("Score")
        .cancelable(true)
        .items(scoreSelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {

            int score = which + scoreStart;

            if (round.holeSet().includes(holeNumber)) {
              callback.holeScoreUpdated(round.holeScore(holeNumber).score(score));
            }

          }
        }).show();
  }

  public static void showPuttsSelectionDialog(final Activity activity, final Round round,
                                              final int holeNumber,
                                              final HoleScoreDialogCallback callback) {

    String[] puttSelections = new String[6];
    for (int putts = 0; putts < puttSelections.length; putts++) {
      puttSelections[putts] = String.valueOf(putts);
    }

    new MaterialDialog.Builder(activity)
        .title("Putts")
        .cancelable(true)
        .items(puttSelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {
            if (round.holeSet().includes(holeNumber)) {
              callback.holeScoreUpdated(round.holeScore(holeNumber).putts(which));
            }
          }
        }).show();
  }

  public static void showPenaltiesSelectionDialog(final Activity activity, final Round round,
                                                  final int holeNumber,
                                                  final HoleScoreDialogCallback callback) {

    String[] penaltySelections = new String[10];
    for (int penalties = 0; penalties < penaltySelections.length; penalties++) {
      penaltySelections[penalties] = String.valueOf(penalties);
    }

    new MaterialDialog.Builder(activity)
        .title("Penalty Strokes")
        .cancelable(true)
        .items(penaltySelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {
            if (round.holeSet().includes(holeNumber)) {
              callback.holeScoreUpdated(round.holeScore(holeNumber).penaltyStrokes(which));
            }
          }
        }).show();
  }

}
