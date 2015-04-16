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
import org.cranst0n.dogleg.android.utils.Units;
import org.cranst0n.dogleg.android.utils.Vibration;
import org.cranst0n.dogleg.android.utils.nfc.Nfc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RoundPlayHoleViewFragment extends BaseFragment {

  private static final String Tag = RoundPlayHoleViewFragment.class.getSimpleName();

  private RoundPlayFragment.PlayRoundListener playRoundListener;

  private NfcAdapter nfcAdapter;
  private boolean autoManageStrokes = true;

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

    addShot(Nfc.readClubTag(tag));
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

      HoleScore currentHoleScore = currentHoleScore();
      boolean holeIncluded = round().holeSet().includes(currentHole);

      currentHoleScoreView.setText(String.valueOf(currentHoleScore.score));
      currentHolePuttsView.setText(String.valueOf(currentHoleScore.putts));
      currentHolePenaltiesView.setText(String.valueOf(currentHoleScore.penaltyStrokes));
      currentHoleFairwayHitBox.setChecked(currentHoleScore.fairwayHit);
      currentHoleGirBox.setChecked(currentHoleScore.gir);

      currentHoleScoreIncrementBtn.setEnabled(holeIncluded);
      currentHoleScoreDecrementBtn.setEnabled(holeIncluded);
      currentHolePuttsIncrementBtn.setEnabled(holeIncluded);
      currentHolePuttsDecrementBtn.setEnabled(holeIncluded);
      currentHolePenaltiesIncrementBtn.setEnabled(holeIncluded);
      currentHolePenaltiesDecrementBtn.setEnabled(holeIncluded);
      currentHoleFairwayHitBox.setEnabled(holeIncluded && currentHoleRating().par > 3);
      currentHoleGirBox.setEnabled(holeIncluded);

      featureListAdapter.features.clear();
      featureListAdapter.features.addAll(currentHoleScore.hole.displayableFeatures());
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
          ((FeatureListAdapter.ViewHolder)holeFeatureList.getChildAt(ix).getTag())
              .updateDistances(location);
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

  private void addShot(final Club club) {

    if (club == Club.FinishHole) {

      if (lastKnownLocation() != null) {
        if (!currentHoleScore().shots.isEmpty()) {

          Shot lastShot = currentHoleScore().shots.get(currentHoleScore().shots.size() - 1);
          Shot finishedShot = lastShot.locationEnd(LatLon.fromLocation(lastKnownLocation()));

          HoleScore finishedScore = currentHoleScore().removeShot(lastShot).withShot(finishedShot);

          if (playRoundListener != null) {
            playRoundListener.updateScore(finishedScore);
          }
        }
      }

      if (playRoundListener != null) {
        playRoundListener.nextHole();
      }

    } else if (lastKnownLocation() != null) {

      switch (club) {
        case Unknown: {
          SnackBars.showSimple(activity, "Can't add shot: unknown club type.");
          break;
        }
        default: {

          Shot newShot = new Shot(-1, currentHoleScore().shots.size() + 1, club,
              LatLon.fromLocation(lastKnownLocation()), LatLon.fromLocation
              (lastKnownLocation()), currentHoleScore().id);

          HoleScore newScore = currentHoleScore().withShot(newShot);

          if (autoManageStrokes) {
            newScore = newScore.addStroke();
            if (club == Club.Putter) {
              newScore = newScore.addPutt();
            }
          }

          if (playRoundListener != null) {
            playRoundListener.updateScore(newScore);
            Vibration.vibrate();
          }

          break;
        }
      }
    } else {
      SnackBars.showSimple(activity, "Can't add shot without current location.");
    }
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

    currentHoleFairwayHitBox = (CheckBox) playRoundHoleView.findViewById(R.id
        .current_hole_fairway_hit);
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

    currentHoleFairwayHitBox.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().fairwayHit(((CheckBox) view).isChecked()));
        }
      }
    });

    currentHoleGirBox.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        if (playRoundListener != null) {
          playRoundListener.updateScore(currentHoleScore().gir(((CheckBox) view).isChecked()));
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
            .title("Shot Settings")
            .customView(R.layout.dialog_shot_settings, true)
            .positiveText("Ok")
            .build();

        final CheckBox autoStrokeBox = (CheckBox) dialog.getCustomView().findViewById(R.id
            .auto_shot_box);

        final CheckBox keepScreenOnBox = (CheckBox) dialog.getCustomView().findViewById(R.id
            .keep_screen_on_box);

        autoStrokeBox.setChecked(autoManageStrokes);
        keepScreenOnBox.setChecked((activity.getWindow().getAttributes().flags & WindowManager
            .LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);

        autoStrokeBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            autoManageStrokes = isChecked;
          }
        });
        keepScreenOnBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(final CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
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
                  addShot(Club.values()[i]);
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
    public boolean isEnabled(final int position) {
      return false;
    }

    @Override
    public int getCount() {
      return features.size();
    }

    @Override
    public Object getItem(final int position) {
      return features.get(position);
    }

    @Override
    public long getItemId(final int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

      ViewHolder holder;

      if(convertView == null) {

        LayoutInflater mInflater = LayoutInflater.from(context);

        convertView = mInflater.inflate(R.layout.item_hole_feature, null);
        holder = new ViewHolder();
        convertView.setTag(holder);

        convertView.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return true;
          }
        });

        holder.iconView = (ImageView) convertView.findViewById(R.id.hole_feature_image);
        holder.nameView = (TextView) convertView.findViewById(R.id.feature_name);
        holder.elevationDifferenceView = (TextView) convertView.findViewById(R.id.feature_elevation_difference);

        holder.distanceView1Label = (TextView) convertView.findViewById(R.id.feature_distance_1_label);
        holder.distanceView2Label = (TextView) convertView.findViewById(R.id.feature_distance_2_label);
        holder.distanceView3Label = (TextView) convertView.findViewById(R.id.feature_distance_3_label);
        holder.distanceView1 = (TextView) convertView.findViewById(R.id.feature_distance_1);
        holder.distanceView2 = (TextView) convertView.findViewById(R.id.feature_distance_2);
        holder.distanceView3 = (TextView) convertView.findViewById(R.id.feature_distance_3);

      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.holeFeature = features.get(position);

      holder.init();
      holder.updateDistances(lastKnownLocation());

      return convertView;
    }

    private final Context context;
    private final List<HoleFeature> features;

    private class ViewHolder {

      private HoleFeature holeFeature;

      private ImageView iconView;
      private TextView nameView;

      private TextView distanceView1Label;
      private TextView distanceView2Label;
      private TextView distanceView3Label;
      private TextView distanceView1;
      private TextView distanceView2;
      private TextView distanceView3;

      private TextView elevationDifferenceView;

      public void init() {
        nameView.setText(holeFeature.name);
        iconView.setImageResource(featureIcon(holeFeature));
      }

      public void updateDistances(final Location location) {

        if (location != null) {

          distanceView1Label.setVisibility(holeFeature.coordinates.length >= 1 ? View.VISIBLE :
              View.GONE);
          distanceView2Label.setVisibility(holeFeature.coordinates.length >= 2 ? View.VISIBLE : View.GONE);
          distanceView3Label.setVisibility(holeFeature.coordinates.length >= 3 ? View.VISIBLE : View.GONE);
          distanceView1.setVisibility(distanceView1Label.getVisibility());
          distanceView2.setVisibility(distanceView2Label.getVisibility());
          distanceView3.setVisibility(distanceView3Label.getVisibility());

          if (holeFeature.coordinates.length == 1) {
            distanceView1Label.setText("Reach:");
            distanceView1.setText(distanceText(location, holeFeature.coordinates[0].toLocation()));
          } else if (holeFeature.coordinates.length == 2) {
            distanceView1Label.setText("Reach:");
            distanceView2Label.setText("Carry:");
            distanceView1.setText(distanceText(location, holeFeature.coordinates[0].toLocation()));
            distanceView2.setText(distanceText(location, holeFeature.coordinates[1].toLocation()));
          } else if (holeFeature.coordinates.length >= 3) {
            distanceView1Label.setText("Front:");
            distanceView2Label.setText("Middle:");
            distanceView3Label.setText("Back:");
            distanceView1.setText(distanceText(location, holeFeature.coordinates[0].toLocation()));
            distanceView2.setText(distanceText(location, holeFeature.coordinates[1].toLocation()));
            distanceView3.setText(distanceText(location, holeFeature.coordinates[2].toLocation()));
          }

          if (location.hasAltitude()) {

            double elevationDiff = holeFeature.center().altitude - location.getAltitude();
            int feet = (int) Math.round(Units.metersToFeet(elevationDiff));

            if (feet > 0) {
              elevationDifferenceView.setText(String.format("+%dft", feet));
            } else {
              elevationDifferenceView.setText(String.format("%dft", feet));
            }

          } else {
            elevationDifferenceView.setText("");
          }
        }
      }

      private String distanceText(final Location fromLocation, final Location toLocation) {
        double meters = fromLocation.distanceTo(toLocation);
        int yards = (int) Math.round(Units.metersToYards(meters));
        return String.valueOf(yards);
      }

      private int featureIcon(final HoleFeature feature) {
        if (feature.name.toLowerCase().contains("waste")) {
          return R.drawable.waste_area;
        } else if (feature.name.toLowerCase().contains("bunker") || feature.name.toLowerCase().contains("sand") || feature.name.toLowerCase().contains("trap")) {
          return R.drawable.sand;
        } else if (feature.name.toLowerCase().contains("water") || feature.name.toLowerCase().contains("creek")) {
          return R.drawable.water;
        } else if (feature.name.toLowerCase().contains("green") || feature.name.toLowerCase().contains("fairway")) {
          return R.drawable.grass;
        } else if (feature.name.toLowerCase().contains("dogleg")) {
          return R.drawable.dogleg;
        } else if (feature.name.toLowerCase().contains("cart") && feature.name.toLowerCase().contains("path")) {
          return R.drawable.cart_path;
        } else {
          return R.mipmap.ic_launcher;
        }
      }
    }
  }

  class ShotListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Shot> shots;

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
    public long getItemId(final int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

      ViewHolder holder;

      if(convertView == null) {

        LayoutInflater mInflater = LayoutInflater.from(context);

        convertView = mInflater.inflate(R.layout.item_shot, null);
        holder = new ViewHolder();
        convertView.setTag(holder);

        convertView.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return true;
          }
        });

        holder.clubNameView = (TextView) convertView.findViewById(R.id.club_name);
        holder.distanceView = (TextView) convertView.findViewById(R.id.shot_distance);
        holder.removeShotButton = (ImageButton) convertView.findViewById(R.id.remove_shot_button);

      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      final Shot itemShot = shots.get(position);

      holder.clubNameView.setText(itemShot.club.name);

      if (itemShot.distanceMeters() > 0) {
        switch (itemShot.club) {
          case Putter: {
            holder.distanceView.setText(String.format("%s feet", Math.round(itemShot.distanceFeet())));
            break;
          }
          default: {
            holder.distanceView.setText(String.format("%s yards", Math.round(itemShot.distanceYards())));
            break;
          }
        }
        holder.distanceView.setVisibility(View.VISIBLE);
      } else {
        holder.distanceView.setVisibility(View.GONE);
      }

      holder.removeShotButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
          if (playRoundListener != null) {

            HoleScore newScore = currentHoleScore().removeShot(itemShot);

            if (autoManageStrokes) {
              newScore = newScore.subtractStroke();

              if (itemShot.club == Club.Putter) {
                newScore = newScore.subtractPutt();
              }
            }

            playRoundListener.updateScore(newScore);
          }
        }
      });

      return convertView;
    }

    private class ViewHolder {

      private TextView clubNameView;
      private TextView distanceView;
      private ImageButton removeShotButton;

    }
  }
}
