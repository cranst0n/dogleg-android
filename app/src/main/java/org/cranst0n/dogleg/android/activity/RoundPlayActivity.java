package org.cranst0n.dogleg.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.fragment.RoundPlayFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayHoleViewFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayMapFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayScorecardFragment;
import org.cranst0n.dogleg.android.model.Hole;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Locations;
import org.cranst0n.dogleg.android.utils.Threads;
import org.cranst0n.dogleg.android.views.HoleScoreDialogs;
import org.cranst0n.dogleg.android.views.RoundSettingsDialog;

import java.util.concurrent.Callable;

public class RoundPlayActivity extends BaseActivity implements LocationListener,
    RoundPlayFragment.PlayRoundListener {

  private Bus bus;

  private Location lastKnownLocation;
  private User currentUser = app.user();

  private Rounds rounds;

  @Nullable
  private Round round;
  private int currentHole = 1;

  private RoundPlayFragment playRoundFragment;
  private RoundPlayHoleViewFragment holeViewFragment;
  private RoundPlayScorecardFragment scorecardFragment;
  private RoundPlayMapFragment mapFragment;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    holeViewFragment = new RoundPlayHoleViewFragment();
    scorecardFragment = new RoundPlayScorecardFragment();
    mapFragment = new RoundPlayMapFragment();

    playRoundFragment = RoundPlayFragment.instance(scorecardFragment, holeViewFragment,
        mapFragment);

    if (savedInstanceState == null) {
      getSupportFragmentManager().
          beginTransaction().
          add(R.id.activity_base_content_frame, playRoundFragment).
          commit();
    }

    rounds = new Rounds(this);

    drawerFragment.setDrawerIndicatorEnabled(false);

    if (savedInstanceState == null) {

      try {

        Round initRound = Json.pimpedGson().fromJson(getIntent().getStringExtra(Round.class
            .getCanonicalName()), Round.class);

        if (initRound != null) {
          setRound(initRound);
          showRoundSettings();
        } else {
          finishWithDialog("No round to start...exiting.");
        }

      } catch (final Exception exception) {
        Log.e(getClass().getSimpleName(), "Erros starting round.", exception);
        finishWithDialog("Error starting round: " + exception.getMessage());
      }
    } else {

      Round savedRound = Json.pimpedGson().fromJson(
          savedInstanceState.getString(Round.class.getCanonicalName()), Round.class);
      int savedCurrentHole = savedInstanceState.getInt(Integer.class.getCanonicalName());

      setRound(savedRound);
      currentHole = savedCurrentHole;
    }

    if (round != null) {
      getSupportActionBar().setTitle(round.course.name);
    }


    startLocationUpdates();
  }

  private void finishWithDialog(@NonNull final String message) {
    new MaterialDialog.Builder(RoundPlayActivity.this).
        content(message).
        dismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(final DialogInterface dialogInterface) {
            finish();
          }
        }).build().show();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
    Ion.getDefault(this).cancelAll(this);

    stopLocationUpdates();
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(Round.class.getCanonicalName(), Json.pimpedGson().toJson(round));
    outState.putInt(Integer.class.getCanonicalName(), currentHole);
  }

  private void startLocationUpdates() {

    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      new MaterialDialog.Builder(this)
          .content("GPS appears to be turned off. You can turn it on via your 'Location Settings'.")
          .negativeText(android.R.string.cancel)
          .positiveText("Location Settings")
          .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(final MaterialDialog dialog) {
              startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
          }).build().show();
    }

    final DoglegApplication app = DoglegApplication.application();

    app.googleApiClient().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
      @Override
      public void onConnected(final Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        app.locationProviderApi().requestLocationUpdates(
            app.googleApiClient(), locationRequest, RoundPlayActivity.this);
      }

      @Override
      public void onConnectionSuspended(int i) {

      }
    });
  }

  private void stopLocationUpdates() {
    DoglegApplication app = DoglegApplication.application();
    app.locationProviderApi().removeLocationUpdates(app.googleApiClient(), this);
  }

  @Override
  protected int getLayoutResourceIdentifier() {
    return R.layout.activity_base;
  }

  @Override
  protected int getToolbarTitle() {
    return R.string.app_name;
  }

  @Override
  protected void onNewIntent(final Intent intent) {
    holeViewFragment.onNewIntent(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.play_round_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home: {
        backPressed();
        return true;
      }
      case R.id.action_settings: {
        showRoundSettings();
        return true;
      }
      default: {
        return super.onOptionsItemSelected(item);
      }
    }
  }

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    //Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      backPressed();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  public void onLocationChanged(@NonNull final Location location) {
    locationUpdated(location);
  }

  private void locationUpdated(@NonNull final Location location) {
    if (Locations.isBetterLocation(location, lastKnownLocation, Locations.Precision.HIGH)) {
      lastKnownLocation = location;
      holeViewFragment.locationUpdated(location);
      mapFragment.locationUpdated(location);
    }
  }

  @Nullable
  public Location lastKnownLocation() {
    return lastKnownLocation;
  }

  @Subscribe
  public void newUser(@NonNull final User user) {
    currentUser = user;

    invalidateOptionsMenu();

    if (user.isValid() && round != null) {
      fetchAutoHandicap(round);
    }
  }

  @NonNull
  public User currentUser() {
    return currentUser;
  }

  private void setRound(@NonNull final Round round) {

    this.round = round;

    playRoundFragment.roundUpdated(round);
    scorecardFragment.setRound(round);

    if (round.holeSet().includes(currentHoleNumber())) {
      setHole(currentHoleNumber());
    } else {
      setHole(round.holeSet().holeStart);
    }

    backupRoundData(round);
  }

  @Nullable
  public Round round() {
    return round;
  }

  public int currentHoleNumber() {
    return currentHole;
  }

  @NonNull
  public Hole currentHole() {
    if (round != null) {
      return round.course.holes[currentHole - 1];
    } else {
      return Hole.empty();
    }
  }

  @Override
  public void previousHole() {
    if (currentHole > 1) {
      setHole(currentHole - 1);
    } else if (round != null) {
      setHole(round.course.numHoles);
    }
  }

  @Override
  public void nextHole() {
    if (round != null && currentHoleNumber() < round.course.numHoles) {
      setHole(currentHoleNumber() + 1);
    } else {
      setHole(1);
    }
  }

  @Override
  public void gotoHole(final int holeNumber) {
    if (currentHoleNumber() != holeNumber) {
      setHole(holeNumber);
    }
  }

  private void setHole(final int hole) {
    if (round != null) {
      this.currentHole = Math.max(1, Math.min(hole, round.course.numHoles));
      playRoundFragment.holeUpdated(currentHole);
      holeViewFragment.holeUpdated(currentHole);
      mapFragment.holeUpdated(currentHole);
    }
  }

  @Nullable
  public HoleRating currentHoleRating() {
    if (round != null) {
      return round.rating.holeRating(currentHole);
    }

    return null;
  }

  @NonNull
  public HoleScore currentHoleScore() {
    if (round != null && round.holeSet().includes(currentHole)) {
      HoleScore hs = round.holeScore(currentHole);
      return hs == null ? HoleScore.empty() : hs;
    } else {
      return HoleScore.empty();
    }
  }

  @Override
  public void updateScore(@NonNull final HoleScore holeScore) {
    if (round != null && round.holeSet().includes((holeScore.hole.number))) {
      HoleScore updatedScore = round.updateScore(holeScore);
      scorecardFragment.updateHole(updatedScore);
      playRoundFragment.scoreUpdated(round);
      holeViewFragment.updateHole(updatedScore);

      backupRoundData(round);
    }
  }

  private void fetchAutoHandicap(@NonNull final Round round) {
    // Try to get handicap from server side //
    rounds.handicapRound(round.roundSlope(), round.holeSet().numHoles, round.time).
        onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
          @Override
          public void onSuccess(@NonNull final RoundHandicapResponse value) {
            setRound(round.withAutoHandicap(value.handicap));
          }
        });
  }

  @Override
  public void showScoreSelectionDialog(final int holeNumber) {
    if (round != null) {
      HoleScoreDialogs.showScoreSelectionDialog(this, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
        @Override
        public void holeScoreUpdated(final HoleScore holeScore) {
          updateScore(holeScore);
        }
      });
    }
  }

  @Override
  public void showPuttsSelectionDialog(final int holeNumber) {
    if (round != null) {
      HoleScoreDialogs.showPuttsSelectionDialog(this, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
        @Override
        public void holeScoreUpdated(final HoleScore holeScore) {
          updateScore(holeScore);
        }
      });
    }
  }

  @Override
  public void showPenaltiesSelectionDialog(final int holeNumber) {
    if (round != null) {
      HoleScoreDialogs.showPenaltiesSelectionDialog(this, round, holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
        @Override
        public void holeScoreUpdated(final HoleScore holeScore) {
          updateScore(holeScore);
        }
      });
    }
  }

  private void backPressed() {
    confirmExit();
  }

  private void confirmExit() {
    new MaterialDialog.Builder(this)
        .content("Are you sure you want to exit this round? You will lose all data.")
        .positiveText("Yes")
        .negativeText("No")
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog dialog) {

            if (round != null) {
              rounds.clearBackupRoundData(round.id);
            }

            RoundPlayActivity.this.finish();
          }
        })
        .show();
  }

  private void showRoundSettings() {
    if (round != null) {
      new RoundSettingsDialog(round, this, new RoundSettingsDialog.RoundSettingsCallback() {
        @Override
        public void settingsUpdated(@NonNull final Round round) {
          setRound(round);
        }
      }).show();
    }
  }

  private void backupRoundData(@NonNull final Round round) {
    Threads.background(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return rounds.backupRoundData(round);
      }
    });
  }
}
