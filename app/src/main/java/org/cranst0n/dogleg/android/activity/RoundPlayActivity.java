package org.cranst0n.dogleg.android.activity;

import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.BackendMessage;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.fragment.RoundPlayFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayHoleViewFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayMapFragment;
import org.cranst0n.dogleg.android.fragment.RoundPlayScorecardFragment;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.Hole;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Locations;
import org.cranst0n.dogleg.android.views.HoleScoreDialogs;
import org.cranst0n.dogleg.android.views.RoundSettingsDialog;

public class RoundPlayActivity extends BaseActivity implements LocationListener,
    RoundPlayFragment.PlayRoundListener {

  private Bus bus;

  private Location lastKnownLocation;
  private int locationStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
  private User currentUser = DoglegApplication.appUser();

  private Rounds rounds;
  private Courses courses;

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
    playRoundFragment = RoundPlayFragment.instance(holeViewFragment, scorecardFragment, mapFragment);

    if (savedInstanceState == null) {
      getSupportFragmentManager().
          beginTransaction().
          add(R.id.activity_base_content_frame, playRoundFragment).
          commit();
    }

    rounds = new Rounds(this);
    courses = new Courses(this);

    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    drawerFragment.setDrawerIndicatorEnabled(false);

    BackendResponse<JsonObject, Course> courseResponse;

    if (savedInstanceState == null) {

      long courseId = (Long) getIntent().getSerializableExtra(
          getResources().getString(R.string.intent_course_id_key));

      final MaterialDialog progressDialog = Dialogs.showBusyDialog(this, "Starting round...");

      courseResponse = courses.info(courseId).
          onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
            @Override
            public void onSuccess(final Course value) {
              setRound(Round.create(-1, currentUser(), value, HoleSet.available(value)[0]));
              showRoundSettings();
            }
          }).
          onError(new BackendResponse.BackendErrorListener() {
            @Override
            public void onError(BackendMessage message) {
              progressDialog.dismiss();
              new MaterialDialog.Builder(RoundPlayActivity.this).
                  content("Error starting round: " + message.message).
                  dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialogInterface) {
                      finish();
                    }
                  }).build().show();
            }
          }).
          onException(new BackendResponse.BackendExceptionListener() {
            @Override
            public void onException(final Exception exception) {
              progressDialog.dismiss();
              new MaterialDialog.Builder(RoundPlayActivity.this).
                  content("Error starting round: " + exception.getMessage()).
                  dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(final DialogInterface dialogInterface) {
                      finish();
                    }
                  }).build().show();
            }
          }).
          onFinally(new BackendResponse.BackendFinallyListener() {
            @Override
            public void onFinally() {
              progressDialog.dismiss();
            }
          });

    } else {

      Round savedRound = Json.pimpedGson().fromJson(
          savedInstanceState.getString(Round.class.getCanonicalName()), Round.class);
      int savedCurrentHole = savedInstanceState.getInt(Integer.class.getCanonicalName());

      setRound(savedRound);
      currentHole = savedCurrentHole;

      courseResponse = BackendResponse.pure(savedRound.course);
    }

    courseResponse.onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
      @Override
      public void onSuccess(final Course value) {
        getSupportActionBar().setTitle(value.name);
        startLocationUpdates();
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
    Ion.getDefault(this).cancelAll(this);

    stopLocationUpdates();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(Round.class.getCanonicalName(), Json.pimpedGson().toJson(round));
    outState.putInt(Integer.class.getCanonicalName(), currentHole);
  }

  private void startLocationUpdates() {

    final DoglegApplication app = DoglegApplication.application();

    app.googleApiClient().registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
      @Override
      public void onConnected(final Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
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
  protected int getTitleToolBar() {
    return R.string.app_name;
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.login_menu, menu);
    getMenuInflater().inflate(R.menu.play_round_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    menu.setGroupVisible(R.id.group_logged_out, !currentUser.isValid());
    menu.setGroupVisible(R.id.group_logged_in, currentUser.isValid());
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home: {
        backPressed();
        return true;
      }
      case R.id.action_login: {
        Dialogs.showLoginDialog(this);
        return true;
      }
      case R.id.action_logout: {
        new Authentication(this).logout();
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
  public void onLocationChanged(final Location location) {
    locationUpdated(location);
  }

  private void locationUpdated(final Location location) {
    if (Locations.isBetterLocation(location, lastKnownLocation)) {
      lastKnownLocation = location;
      holeViewFragment.locationUpdated(location);
      mapFragment.locationUpdated(location);
    }
  }

  public Location lastKnownLocation() {
    return lastKnownLocation;
  }

  public int locationStatus() {
    return locationStatus;
  }

  @Subscribe
  public void newUser(final User user) {
    currentUser = user;

    invalidateOptionsMenu();

    if (user.isValid() && round != null) {
      fetchAutoHandicap(round);
    }
  }

  public User currentUser() {
    return currentUser;
  }

  private void setRound(final Round round) {
    this.round = round;
    playRoundFragment.roundUpdated(round);
    scorecardFragment.setRound(round);

    if (round.holeSet().includes(currentHoleNumber())) {
      setHole(currentHoleNumber());
    } else {
      setHole(round.holeSet().holeStart);
    }
  }

  public Round round() {
    return round;
  }

  public int currentHoleNumber() {
    return currentHole;
  }

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
    } else {
      setHole(round.course.numHoles);
    }
  }

  @Override
  public void nextHole() {
    if (currentHoleNumber() < round().course.numHoles) {
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
    this.currentHole = hole;
    playRoundFragment.holeUpdated(currentHole);
    holeViewFragment.holeUpdated(currentHole);
    mapFragment.holeUpdated(currentHole);
  }

  public HoleRating currentHoleRating() {
    return round.rating.holeRating(currentHole);
  }

  public HoleScore currentHoleScore() {
    if (round != null && round.holeSet().includes(currentHole)) {
      return round.holeScore(currentHole);
    } else {
      return HoleScore.empty();
    }
  }

  @Override
  public void updateScore(final HoleScore holeScore) {
    if (round != null && round.holeSet().includes((holeScore.hole.number))) {
      HoleScore updatedScore = round.updateScore(holeScore);
      scorecardFragment.updateHole(updatedScore);
      playRoundFragment.scoreUpdated(updatedScore);
      holeViewFragment.updateHole(updatedScore);
    }
  }

  private void fetchAutoHandicap(final Round round) {
    // Try to get handicap from server side //
    rounds.handicapRound(round.roundSlope(), round.holeSet().numHoles, round.time).
        onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
          @Override
          public void onSuccess(final RoundHandicapResponse value) {
            setRound(round.withAutoHandicap(value.handicap));
          }
        });
  }

  @Override
  public void showScoreSelectionDialog(final int holeNumber) {
    HoleScoreDialogs.showScoreSelectionDialog(this, round(), holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
      @Override
      public void holeScoreUpdated(final HoleScore holeScore) {
        updateScore(holeScore);
      }
    });
  }

  @Override
  public void showPuttsSelectionDialog(final int holeNumber) {
    HoleScoreDialogs.showPuttsSelectionDialog(this, round(), holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
      @Override
      public void holeScoreUpdated(final HoleScore holeScore) {
        updateScore(holeScore);
      }
    });
  }

  @Override
  public void showPenaltiesSelectionDialog(final int holeNumber) {
    HoleScoreDialogs.showPenaltiesSelectionDialog(this, round(), holeNumber, new HoleScoreDialogs.HoleScoreDialogCallback() {
      @Override
      public void holeScoreUpdated(final HoleScore holeScore) {
        updateScore(holeScore);
      }
    });
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
          public void onPositive(MaterialDialog dialog) {
            RoundPlayActivity.this.finish();
          }
        })
        .show();
  }

  private void showRoundSettings() {
    new RoundSettingsDialog(round, this, new RoundSettingsDialog.RoundSettingsCallback() {
      @Override
      public void settingsUpdated(final Round round) {
        setRound(round);
        fetchAutoHandicap(round);
      }
    }).show();
  }
}
