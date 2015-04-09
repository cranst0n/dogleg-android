package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.RoundPlayActivity;
import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.model.Club;
import org.cranst0n.dogleg.android.model.HoleFeature;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;
import org.cranst0n.dogleg.android.model.Shot;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.utils.nfc.Nfc;
import org.cranst0n.dogleg.android.views.HoleFeatureItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoundPlayHoleViewFragment extends BaseFragment {

  private static final String Tag = RoundPlayHoleViewFragment.class.getSimpleName();

  private RoundPlayFragment.PlayRoundListener playRoundListener;

  private NfcAdapter nfcAdapter;

  private View playRoundHoleView;

  private TextView currentHoleScoreView;
  private ImageButton currentHoleScoreIncrementBtn;
  private ImageButton currentHoleScoreDecrementBtn;
  private TextView currentHolePuttsView;
  private ImageButton currentHolePuttsIncrementBtn;
  private ImageButton currentHolePuttsDecrementBtn;
  private TextView currentHolePenaltiesView;
  private ImageButton currentHolePenaltiesIncrementBtn;
  private ImageButton currentHolePenaltiesDecrementBtn;
  private CheckBox currentHoleFairwayHitBox;
  private CheckBox currentHoleGirBox;

  private TextView statsPuttsView;
  private TextView statsPuttsAverageView;
  private TextView statsFairwayHitView;
  private TextView statsGirView;
  private TextView statsPar3AverageView;
  private TextView statsPar4AverageView;
  private TextView statsPar5AverageView;

  private TextView gpsStatusView;
  private ListView holeFeatureList;
  private FeatureListAdapter featureListAdapter;

  private ImageButton shotSettingsButton;
  private ImageButton shotAddButton;
  private ListView shotList;
  private ShotListAdapter shotListAdapter;

  private Button submitRoundButton;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    if (activity instanceof RoundPlayFragment.PlayRoundListener) {
      playRoundListener = (RoundPlayFragment.PlayRoundListener) activity;
    }

    initNfc();
  }

  @Override
  public void onPause() {
    super.onPause();

    if (nfcAdapter != null) {
      nfcAdapter.disableForegroundDispatch(activity);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (nfcAdapter != null) {
      Nfc.enableForegroundDispatch(nfcAdapter, activity);
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    playRoundHoleView = inflater.inflate(R.layout.fragment_round_play_hole_view, container, false);

    setHasOptionsMenu(true);

    findViews();
    attachListeners();

    return playRoundHoleView;
  }

  private void initNfc() {

    nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

    if (nfcAdapter == null) {
      SnackBars.showSimple(activity, "This device doesn't seem to support NFC.");
    } else {

      if (!nfcAdapter.isEnabled()) {
        SnackBars.showSimple(activity, "NFC is disabled!");
      }
    }
  }

  public void onNewIntent(final Intent intent) {

    android.nfc.Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

    Log.d(Tag, "New NFC tag: [" + tag + "]");

    Club club = Nfc.readClubTag(tag);

    if (club != Club.Unknown) {
      Shot newShot = new Shot(-1, currentHoleScore().shots.size() + 1, club,
          LatLon.fromLocation(lastKnownLocation()), LatLon.fromLocation
          (lastKnownLocation()), currentHoleScore().id);

      if (playRoundListener != null) {
        playRoundListener.updateScore(currentHoleScore().withShot(newShot));
      }
    } else {
      SnackBars.showSimple(activity, "Can't add shot: unknown club type.");
    }
  }

  public void locationUpdated(final Location location) {
    updateGpsDisplay(location);
  }

  private User currentUser() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentUser();
    }

    return User.NO_USER;
  }

  private Location lastKnownLocation() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).lastKnownLocation();
    }

    return null;
  }

  private Round round() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).round();
    }

    return null;
  }

  private int currentHoleNumber() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentHoleNumber();
    }

    return 1;
  }

  private HoleRating currentHoleRating() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentHoleRating();
    }

    return null;
  }

  private HoleScore currentHoleScore() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentHoleScore();
    }

    return null;
  }

  public void holeUpdated(final int currentHole) {

    if (isAdded()) {

      currentHoleScoreView.setText(String.valueOf(currentHoleScore().score));
      currentHolePuttsView.setText(String.valueOf(currentHoleScore().putts));
      currentHolePenaltiesView.setText(String.valueOf(currentHoleScore().penaltyStrokes));
      currentHoleFairwayHitBox.setChecked(currentHoleScore().fairwayHit);
      currentHoleGirBox.setChecked(currentHoleScore().gir);

      currentHoleScoreIncrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHoleScoreDecrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHolePuttsIncrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHolePuttsDecrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHolePenaltiesIncrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHolePenaltiesDecrementBtn.setEnabled(round().holeSet().includes(currentHole));
      currentHoleFairwayHitBox.setEnabled(
          round().holeSet().includes(currentHole) && currentHoleRating().par > 3);
      currentHoleGirBox.setEnabled(round().holeSet().includes(currentHole));

      featureListAdapter.features.clear();
      featureListAdapter.features.addAll(currentHoleScore().hole.displayableFeatures());
      featureListAdapter.notifyDataSetChanged();

      updateStats();
      updateGpsDisplay(lastKnownLocation());
      updateShotList();
    }
  }

  public void updateHole(final HoleScore holeScore) {
    if (isAdded() && currentHoleNumber() == holeScore.hole.number) {
      currentHoleScoreView.setText(String.valueOf(holeScore.score));
      currentHolePuttsView.setText(String.valueOf(holeScore.putts));
      currentHolePenaltiesView.setText(String.valueOf(holeScore.penaltyStrokes));
      currentHoleFairwayHitBox.setChecked(holeScore.fairwayHit);
      currentHoleGirBox.setChecked(holeScore.gir);

      updateShotList();
    }

    updateStats();
  }

  private void updateStats() {
    if (isAdded()) {

      RoundStats stats = round().stats();

      statsPuttsView.setText(String.valueOf(stats.putts));
      statsPuttsAverageView.setText(String.format("%.2f", stats.puttAverage));
      statsFairwayHitView.setText(String.format("%.2f%%", stats.fairwayHitPercentage * 100));
      statsGirView.setText(String.format("%.2f%%", stats.girPercentage * 100));
      statsPar3AverageView.setText(String.format("%.2f", stats.par3ScoringAverage));
      statsPar4AverageView.setText(String.format("%.2f", stats.par4ScoringAverage));
      statsPar5AverageView.setText(String.format("%.2f", stats.par5ScoringAverage));
    }
  }

  private void updateGpsDisplay(final Location location) {

    if (isAdded()) {
      if (location != null) {

        gpsStatusView.setText(String.format("+/- %.0fm", location.getAccuracy()));

        Collections.sort(featureListAdapter.features, new Comparator<HoleFeature>() {
          @Override
          public int compare(final HoleFeature lhs, final HoleFeature rhs) {

            double lhsDist = location.distanceTo(lhs.center().toLocation());
            double rhsDist = location.distanceTo(rhs.center().toLocation());

            if (lhsDist < rhsDist) {
              return -1;
            } else if (rhsDist > lhsDist) {
              return 1;
            } else {
              return 0;
            }
          }
        });

        for (int ix = 0; ix < holeFeatureList.getChildCount(); ix++) {
          ((HoleFeatureItem) holeFeatureList.getChildAt(ix)).updateDistances(location);
        }

        featureListAdapter.notifyDataSetChanged();

      } else {
        gpsStatusView.setText("Searching...");
      }
    }
  }

  private void updateShotList() {
    shotListAdapter.shots.clear();
    shotListAdapter.shots.addAll(currentHoleScore().shots);
    shotListAdapter.notifyDataSetChanged();
  }

  private void findViews() {

    currentHoleScoreView = (TextView) playRoundHoleView.findViewById(R.id.current_hole_score);
    currentHoleScoreIncrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_score_increment);
    currentHoleScoreDecrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_score_decrement);

    currentHolePuttsView = (TextView) playRoundHoleView.findViewById(R.id.current_hole_putts);
    currentHolePuttsIncrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_putts_increment);
    currentHolePuttsDecrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_putts_decrement);

    currentHolePenaltiesView = (TextView) playRoundHoleView.findViewById(R.id.current_hole_penalties);
    currentHolePenaltiesIncrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_penalties_increment);
    currentHolePenaltiesDecrementBtn =
        (ImageButton) playRoundHoleView.findViewById(R.id.current_hole_penalties_decrement);

    currentHoleFairwayHitBox = (CheckBox) playRoundHoleView.findViewById(R.id.current_hole_fairway_hit);
    currentHoleGirBox = (CheckBox) playRoundHoleView.findViewById(R.id.current_hole_gir);

    statsPuttsView = (TextView) playRoundHoleView.findViewById(R.id.round_putts_stat);
    statsPuttsAverageView = (TextView) playRoundHoleView.findViewById(R.id.round_putts_average_stat);
    statsFairwayHitView = (TextView) playRoundHoleView.findViewById(R.id.round_fairway_stats);
    statsGirView = (TextView) playRoundHoleView.findViewById(R.id.round_gir_stats);
    statsPar3AverageView = (TextView) playRoundHoleView.findViewById(R.id.round_par3_stats);
    statsPar4AverageView = (TextView) playRoundHoleView.findViewById(R.id.round_par4_stats);
    statsPar5AverageView = (TextView) playRoundHoleView.findViewById(R.id.round_par5_stats);

    gpsStatusView = (TextView) playRoundHoleView.findViewById(R.id.gps_status);

    holeFeatureList = (ListView) playRoundHoleView.findViewById(R.id.current_hole_feature_list);
    featureListAdapter = new FeatureListAdapter(
        context, new ArrayList<>(currentHoleScore().hole.displayableFeatures()));
    holeFeatureList.setAdapter(featureListAdapter);

    shotSettingsButton = (ImageButton) playRoundHoleView.findViewById(R.id.shot_settings_button);
    shotAddButton = (ImageButton) playRoundHoleView.findViewById(R.id.shot_add_button);
    shotList = (ListView) playRoundHoleView.findViewById(R.id.current_hole_shot_list);
    shotListAdapter = new ShotListAdapter(context, new ArrayList<Shot>());
    shotList.setAdapter(shotListAdapter);

    submitRoundButton = (Button) playRoundHoleView.findViewById(R.id.submit_round_button);

    if (round() != null) {
      holeUpdated(currentHoleNumber());
    }
  }

  private void attachListeners() {

    currentHoleScoreIncrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().addStroke());
        }
      }
    });

    currentHoleScoreView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.showScoreSelectionDialog(currentHoleNumber());
        }
      }
    });

    currentHoleScoreDecrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().subtractStroke());
        }
      }
    });

    currentHolePuttsIncrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().addPutt());
        }
      }
    });

    currentHolePuttsView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.showPuttsSelectionDialog(currentHoleNumber());
        }
      }
    });

    currentHolePuttsDecrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().subtractPutt());
        }
      }
    });

    currentHolePenaltiesIncrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().addPenaltyStroke());
        }
      }
    });

    currentHolePenaltiesView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.showPenaltiesSelectionDialog(currentHoleNumber());
        }
      }
    });

    currentHolePenaltiesDecrementBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().subtractPenaltyStroke());
        }
      }
    });

    currentHoleFairwayHitBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().fairwayHit(isChecked));
        }
      }
    });

    currentHoleGirBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().gir(isChecked));
        }
      }
    });

    holeFeatureList.setOnTouchListener(new View.OnTouchListener() {
      // Setting on Touch Listener for handling the touch inside ScrollView
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        // Disallow the touch request for parent scroll on touch of child view
        v.getParent().requestDisallowInterceptTouchEvent(true);
        return false;
      }
    });

    shotSettingsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
            .title("Shot Caddy Settings")
            .customView(R.layout.dialog_shot_settings, true)
            .positiveText("Ok")
            .negativeText(android.R.string.cancel)
            .build();

        final CheckBox keepScreenOnBox = (CheckBox) dialog.getCustomView().findViewById(R.id
            .keep_screen_on_box);

        keepScreenOnBox.setChecked((activity.getWindow().getAttributes().flags & WindowManager
            .LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);

        keepScreenOnBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(final CompoundButton compoundButton, boolean isChecked) {
            if(isChecked) {
              activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
              activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
          }
        });

        dialog.show();
      }
    });

    shotAddButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {

        if (lastKnownLocation() != null) {

          final String[] clubNames = new String[Club.values().length];
          for (int ix = 0; ix < Club.values().length; ix++) {
            clubNames[ix] = Club.values()[ix].name;
          }

          new MaterialDialog.Builder(activity)
              .title("Select Club")
              .items(clubNames)
              .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                @Override
                public boolean onSelection(final MaterialDialog materialDialog, final View view,
                                           final int i, final CharSequence charSequence) {

                  Club selectedClub = Club.values()[i];

                  Shot newShot = new Shot(-1, currentHoleScore().shots.size() + 1, selectedClub,
                      LatLon.fromLocation(lastKnownLocation()), LatLon.fromLocation
                      (lastKnownLocation()), currentHoleScore().id);

                  if (playRoundListener != null) {
                    playRoundListener.updateScore(currentHoleScore().withShot(newShot));
                  }

                  return true;
                }
              }).show();
        } else {
          SnackBars.showSimple(activity, "Can't add shot without current location.");
        }

      }
    });

    submitRoundButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (currentUser().isValid()) {

          final List<Round.RoundIssue> roundIssues = round().issues();

          if (roundIssues.isEmpty()) {

            new MaterialDialog.Builder(activity)
                .content("Are you sure you want to finish this round?")
                .positiveText("Yes")
                .negativeText("No")
                .callback(new MaterialDialog.ButtonCallback() {
                  @Override
                  public void onPositive(final MaterialDialog dialog) {

                    final MaterialDialog progressDialog =
                        Dialogs.showBusyDialog(activity, "Submitting round...");

                    final long localRoundId = round().id;
                    final Rounds rounds = new Rounds(context);

                    rounds.postRound(round().asUser(currentUser())).
                        onSuccess(new BackendResponse.BackendSuccessListener<Round>() {
                          @Override
                          public void onSuccess(final Round value) {
                            rounds.clearBackupRoundData(localRoundId);
                            activity.finish();
                          }
                        }).
                        onError(new BackendResponse.BackendErrorListener() {
                          @Override
                          public void onError(final BackendMessage message) {
                            Dialogs.showMessageDialog(
                                activity, "Round submission failed: " + message.message);
                          }
                        }).
                        onException(new BackendResponse.BackendExceptionListener() {
                          @Override
                          public void onException(final Exception exception) {
                            Dialogs.showMessageDialog(
                                activity, "Round submission failed: " + exception.getMessage());
                          }
                        }).onFinally(new BackendResponse.BackendFinallyListener() {
                      @Override
                      public void onFinally() {
                        progressDialog.dismiss();
                      }
                    });
                  }
                })
                .show();
          } else {

            String[] dialogItems = new String[roundIssues.size()];
            for (int ix = 0; ix < roundIssues.size(); ix++) {
              dialogItems[ix] = roundIssues.get(ix).message;
            }

            new MaterialDialog.Builder(activity)
                .title("This round has issues.")
                .cancelable(true)
                .items(dialogItems)
                .itemsCallback(new MaterialDialog.ListCallback() {
                  @Override
                  public void onSelection(final MaterialDialog materialDialog, final View view,
                                          final int i, final CharSequence charSequence) {

                    Round.RoundIssue selectedIssue = roundIssues.get(i);

                    if (playRoundListener != null) {
                      playRoundListener.gotoHole(selectedIssue.hole.number);
                    }
                  }
                })
                .show();
          }

        } else {
          new MaterialDialog.Builder(activity)
              .content("You must be logged in to save your round!")
              .positiveText("Login")
              .negativeText(android.R.string.cancel)
              .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                  Dialogs.showLoginDialog(activity);
                }
              }).
              show();
        }
      }
    });
  }

  class FeatureListAdapter extends BaseAdapter {

    public FeatureListAdapter(final Context context, List<HoleFeature> features) {
      this.context = context;
      this.features = features;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      return false;
    }

    @Override
    public int getCount() {
      return features.size();
    }

    @Override
    public Object getItem(int position) {
      return features.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      HoleFeatureItem card = new HoleFeatureItem(context, features.get(position));
      card.updateDistances(lastKnownLocation());

      return card;
    }

    private final Context context;
    private final List<HoleFeature> features;
  }

  class ShotListAdapter extends BaseAdapter {

    public ShotListAdapter(final Context context, final List<Shot> shots) {
      this.context = context;
      this.shots = shots;
    }

    @Override
    public int getCount() {
      return shots.size();
    }

    @Override
    public Object getItem(final int position) {
      return shots.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

      final Shot itemShot = shots.get(position);

      LayoutInflater mInflater =
          (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      convertView = mInflater.inflate(R.layout.item_shot, null);

      ImageView iconView = (ImageView) convertView.findViewById(R.id.shot_image);
      TextView clubNameView = (TextView) convertView.findViewById(R.id.club_name);
      TextView distanceView = (TextView) convertView.findViewById(R.id.shot_distance);
      ImageButton removeShotButton = (ImageButton) convertView.findViewById(R.id.remove_shot_button);

      clubNameView.setText(shots.get(position).club.name);

      if (itemShot.distance() > 0) {
        distanceView.setText(String.format("%s yards", Math.round(itemShot.distance())));
      } else {
        distanceView.setText("");
      }

      removeShotButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
          if (playRoundListener != null) {
            playRoundListener.updateScore(currentHoleScore().removeShot(itemShot));
          }
        }
      });

      return convertView;
    }

    private final Context context;
    private final List<Shot> shots;
  }
}
