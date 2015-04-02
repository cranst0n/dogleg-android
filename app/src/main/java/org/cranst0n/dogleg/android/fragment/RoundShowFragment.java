package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.view.ViewHelper;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.constants.Photos;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Objects;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.views.HoleScoreDialogs;
import org.cranst0n.dogleg.android.views.RoundSettingsDialog;
import org.cranst0n.dogleg.android.views.SimpleCheckbox;

public class RoundShowFragment extends ScorecardFragment implements ObservableScrollViewCallbacks {

  private Rounds rounds;

  private Round originalRound;

  private View roundShowView;

  private ImageView courseImageView;
  private ObservableScrollView scrollView;

  private TextView courseNameView;
  private TextView timeView;
  private TextView ratingView;
  private TextView holeSetView;
  private TextView officialView;
  private TextView handicapView;
  private ImageButton roundSettingsButton;

  private ImageButton editRoundBtn;
  private ImageButton saveRoundBtn;
  private ImageButton cancelEditRoundBtn;
  private ImageButton deleteRoundBtn;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    setToolbarOverlaid(true);
    setEnabled(false);

    rounds = new Rounds(context);

    roundShowView = inflater.inflate(R.layout.fragment_round_show, container, false);

    scrollView = (ObservableScrollView) roundShowView.findViewById(R.id.scroll);
    scrollView.setScrollViewCallbacks(this);

    courseImageView = (ImageView) roundShowView.findViewById(R.id.course_image);

    courseNameView = (TextView) roundShowView.findViewById(R.id.round_details_course_name);
    timeView = (TextView) roundShowView.findViewById(R.id.round_details_time);
    ratingView = (TextView) roundShowView.findViewById(R.id.round_details_rating);
    holeSetView = (TextView) roundShowView.findViewById(R.id.round_details_hole_set);
    officialView = (TextView) roundShowView.findViewById(R.id.round_details_official);
    handicapView = (TextView) roundShowView.findViewById(R.id.round_details_handicap);
    roundSettingsButton = (ImageButton) roundShowView.findViewById(R.id.round_details_settings);

    editRoundBtn = (ImageButton) roundShowView.findViewById(R.id.edit_round_btn);
    saveRoundBtn = (ImageButton) roundShowView.findViewById(R.id.save_round_btn);
    cancelEditRoundBtn = (ImageButton) roundShowView.findViewById(R.id.cancel_edit_round_btn);
    deleteRoundBtn = (ImageButton) roundShowView.findViewById(R.id.delete_round_btn);

    setEditControls(enabled);

    editRoundBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        setEnabled(true);
      }
    });

    cancelEditRoundBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        setRound(Objects.deepCopy(originalRound));
        setEnabled(false);
      }
    });

    saveRoundBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Saving Round...");

        rounds.updateRound(round)
            .onSuccess(new BackendResponse.BackendSuccessListener<Round>() {
              @Override
              public void onSuccess(final Round value) {
                originalRound = Objects.deepCopy(round);
                setEnabled(false);
              }
            })
            .onError(new BackendResponse.BackendErrorListener() {
              @Override
              public void onError(final BackendMessage message) {
                SnackBars.showSimple(activity, message.message);
              }
            })
            .onException(new BackendResponse.BackendExceptionListener() {
              @Override
              public void onException(final Exception exception) {
                SnackBars.showSimple(activity, exception.getMessage());
              }
            })
            .onFinally(new BackendResponse.BackendFinallyListener() {
              @Override
              public void onFinally() {
                busyDialog.dismiss();
              }
            });
      }
    });

    deleteRoundBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        new MaterialDialog.Builder(activity)
            .content("Are you sure you want to delete this round? This can not be undone.")
            .positiveText("Yes")
            .negativeText("No")
            .callback(new MaterialDialog.ButtonCallback() {
              @Override
              public void onPositive(final MaterialDialog dialog) {

                final MaterialDialog busyDialog =
                    Dialogs.showBusyDialog(activity, "Deleting Round...");

                rounds.deleteRound(round.id)
                    .onSuccess(new BackendResponse.BackendSuccessListener<BackendMessage>() {
                      @Override
                      public void onSuccess(final BackendMessage value) {
                        Intent data = new Intent();
                        data.putExtra(Round.class.getCanonicalName(),
                            Json.pimpedGson().toJson(round));

                        activity.setResult(R.integer.round_delete_result, data);

                        activity.finish();
                      }
                    })
                    .onError(new BackendResponse.BackendErrorListener() {
                      @Override
                      public void onError(final BackendMessage message) {
                        SnackBars.showSimple(activity, message.message);
                      }
                    })
                    .onException(new BackendResponse.BackendExceptionListener() {
                      @Override
                      public void onException(final Exception exception) {
                        SnackBars.showSimple(activity, exception.getMessage());
                      }
                    })
                    .onFinally(new BackendResponse.BackendFinallyListener() {
                      @Override
                      public void onFinally() {
                        busyDialog.dismiss();
                      }
                    });
              }
            })
            .show();
      }
    });

    roundSettingsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        if (activity instanceof FragmentActivity) {
          new RoundSettingsDialog(round, (FragmentActivity) activity, new RoundSettingsDialog.RoundSettingsCallback() {
            @Override
            public void settingsUpdated(final Round round) {
              if (round.official && !round.isHandicapOverridden) {

                final MaterialDialog busyDialog = Dialogs.showBusyDialog(activity, "Updating round...");

                rounds.handicapRound(round.roundSlope(), round.holeSet().numHoles, round.time)
                    .onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
                      @Override
                      public void onSuccess(RoundHandicapResponse value) {
                        setRound(round.withAutoHandicap(value.handicap));
                      }
                    })
                    .onFinally(new BackendResponse.BackendFinallyListener() {
                      @Override
                      public void onFinally() {
                        busyDialog.dismiss();
                      }
                    });
              } else {
                setRound(round);
              }
            }
          }).show();
        } else {
          SnackBars.showSimple(activity, "Unable to show settings dialog");
        }
      }
    });

    findViews(roundShowView);

    for (int holeIx = 0; holeIx < numHoles(); holeIx++) {

      final int holeNumber = holeStart() + holeIx;

      holeFieldViews[holeNumber - holeStart()].score.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          if (enabled) {
            HoleScoreDialogs.showScoreSelectionDialog(activity, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
              @Override
              public void holeScoreUpdated(final HoleScore holeScore) {
                round.updateScore(holeScore);
                updateScorecard();
              }
            });
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].putts.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          if (enabled) {
            HoleScoreDialogs.showPuttsSelectionDialog(activity, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
              @Override
              public void holeScoreUpdated(final HoleScore holeScore) {
                round.updateScore(holeScore);
                updateScorecard();
              }
            });
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].penalties.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          if (enabled) {
            HoleScoreDialogs.showPenaltiesSelectionDialog(activity, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
              @Override
              public void holeScoreUpdated(final HoleScore holeScore) {
                round.updateScore(holeScore);
                updateScorecard();
              }
            });
          }
        }
      });

      holeFieldViews[holeNumber - holeStart()].fairwayHit.setOnCheckedChangeListener(new SimpleCheckbox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final SimpleCheckbox buttonView, final boolean isChecked) {
          round.updateScore(round.holeScore(holeNumber).fairwayHit(isChecked));
          updateScorecard();
        }
      });

      holeFieldViews[holeNumber - holeStart()].gir.setOnCheckedChangeListener(new SimpleCheckbox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final SimpleCheckbox buttonView, final boolean isChecked) {
          round.updateScore(round.holeScore(holeNumber).gir(isChecked));
          updateScorecard();
        }
      });
    }

    setToolbarBackground(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.primary)));
    onScrollChanged(scrollView.getCurrentScrollY(), false, false);

    setRound(Json.pimpedGson().fromJson(
        activity.getIntent().getStringExtra(getResources().getString(R.string.intent_round_data_key)),
        Round.class));

    originalRound = Objects.deepCopy(round);

    setActionBarTitle(round.course.name);

    Ion.with(courseImageView)
        .centerCrop()
        .load("android.resource://" + activity.getPackageName() + "/" + Photos.photoFor(round.course.id));

    return roundShowView;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      setRound(Json.pimpedGson().fromJson(
          savedInstanceState.getString(Round.class.getCanonicalName()), Round.class));
    }
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(Round.class.getCanonicalName(), Json.pimpedGson().toJson(round));
  }

  @Override
  public ScorecardFragment setEnabled(boolean enabled) {
    setEditControls(enabled);
    return super.setEnabled(enabled);
  }

  private void setEditControls(final boolean enabled) {
    if (editRoundBtn != null) {
      editRoundBtn.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    if (saveRoundBtn != null) {
      saveRoundBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    if (cancelEditRoundBtn != null) {
      cancelEditRoundBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    if (deleteRoundBtn != null) {
      deleteRoundBtn.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    if (roundSettingsButton != null) {
      roundSettingsButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
  }

  @Override
  protected void updateScorecard() {
    super.updateScorecard();

    if (round != null) {
      courseNameView.setText(round.course.name);
      timeView.setText(round.time.toString("MMM dd yyyy @ hh:mma"));
      ratingView.setText(round.rating.teeName);
      holeSetView.setText(round.holeSet().title);
      officialView.setText(round.official ? "Yes" : "No");
      handicapView.setText(round.official ? String.valueOf(round.handicap()) : "N/a");
    }
  }

  @Override
  public void onScrollChanged(final int scrollY, final boolean firstScroll, final boolean dragging) {
    int baseColor = getResources().getColor(R.color.primary);
    float alpha = 1 - (float) Math.max(0, courseImageView.getHeight() - scrollY) / courseImageView.getHeight();
    setToolbarBackground(ScrollUtils.getColorWithAlpha(alpha, baseColor));
    ViewHelper.setTranslationY(courseImageView, scrollY / 4);
  }

  @Override
  public void onDownMotionEvent() {

  }

  @Override
  public void onUpOrCancelMotionEvent(final ScrollState scrollState) {

  }

}
