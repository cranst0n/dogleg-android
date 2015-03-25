package org.cranst0n.dogleg.android.activity;

import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.api.BaseActivity;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.fragment.PlayRoundFragment;
import org.cranst0n.dogleg.android.fragment.PlayRoundHoleViewFragment;
import org.cranst0n.dogleg.android.fragment.PlayRoundMapFragment;
import org.cranst0n.dogleg.android.fragment.PlayRoundScorecardFragment;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.model.Hole;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Dialogs;
import org.cranst0n.dogleg.android.utils.Locations;
import org.joda.time.DateTime;

import java.util.Arrays;

public class PlayRoundActivity extends BaseActivity implements LocationListener,
    PlayRoundFragment.PlayRoundListener {

  private Bus bus;

  private Location lastKnownLocation;
  private int locationStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
  private User currentUser = DoglegApplication.appUser();

  private Rounds rounds;
  private Courses courses;

  private Round round;
  private int currentHole = 1;

  private PlayRoundFragment playRoundFragment;
  private PlayRoundHoleViewFragment holeViewFragment;
  private PlayRoundScorecardFragment scorecardFragment;
  private PlayRoundMapFragment mapFragment;

  // Settings Dialog
  private Spinner ratingSpinner;
  private Spinner holeSetSpinner;
  private Button pickDateButton;
  private CheckBox officialCheckBox;
  private CheckBox handicapOverrideCheckBox;
  private Spinner handicapOverrideSpinner;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    holeViewFragment = new PlayRoundHoleViewFragment();
    scorecardFragment = new PlayRoundScorecardFragment();
    mapFragment = new PlayRoundMapFragment();
    playRoundFragment = PlayRoundFragment.instance(holeViewFragment, scorecardFragment, mapFragment);

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

      courseResponse = courses.info(courseId).
          onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
            @Override
            public void onSuccess(final Course value) {
              setRound(Round.create(currentUser(), value, HoleSet.available(value)[0]));
              showRoundSettings();
            }
          });
    } else {

      Round savedRound = new Gson().fromJson(
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
    stopLocationUpdates();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(Round.class.getCanonicalName(), new Gson().toJson(round));
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
            app.googleApiClient(), locationRequest, PlayRoundActivity.this);
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
    HoleScore updatedScore = round.updateScore(holeScore);
    scorecardFragment.updateHole(updatedScore);
    playRoundFragment.scoreUpdated(updatedScore);
    holeViewFragment.updateHole(updatedScore);
  }

  private void fetchAutoHandicap(final Round round) {

    // Try to get handicap from server side //
    double slope = round.rating.slope;

    if (round.holeSet() == HoleSet.Front9) {
      slope = round.rating.frontSlope;
    } else if (round.holeSet() == HoleSet.Back9) {
      slope = round.rating.backSlope;
    }

    rounds.handicapRound(slope, round.holeSet().numHoles, round.time).
        onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
          @Override
          public void onSuccess(final RoundHandicapResponse value) {
            setRound(round.withHandicap(value.handicap));
          }
        });
  }

  @Override
  public void showScoreSelectionDialog(final int holeNumber) {
    HoleRating holeRating = round().rating.holeRating(holeNumber);

    final int scoreStart = Math.max(holeRating.par - 3, 1);
    final int scoreEnd = scoreStart + 10;
    String[] scoreSelections = new String[scoreEnd - scoreStart + 1];
    for (int score = scoreStart; score <= scoreEnd; score++) {
      scoreSelections[score - scoreStart] =
          String.format("%d - (%s)", score, HoleScore.scoreToParString(score, holeRating.par));
    }

    new MaterialDialog.Builder(this)
        .title("Score")
        .cancelable(true)
        .items(scoreSelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {

            int score = which + scoreStart;
            updateScore(round.holeScore(holeNumber).score(score));
          }
        }).show();
  }

  @Override
  public void showPuttsSelectionDialog(final int holeNumber) {

    String[] puttSelections = new String[6];
    for (int putts = 0; putts < puttSelections.length; putts++) {
      puttSelections[putts] = String.valueOf(putts);
    }

    new MaterialDialog.Builder(this)
        .title("Putts")
        .cancelable(true)
        .items(puttSelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {
            updateScore(round.holeScore(holeNumber).putts(which));
          }
        }).show();
  }

  @Override
  public void showPenaltiesSelectionDialog(final int holeNumber) {

    String[] penaltySelections = new String[10];
    for (int penalties = 0; penalties < penaltySelections.length; penalties++) {
      penaltySelections[penalties] = String.valueOf(penalties);
    }

    new MaterialDialog.Builder(this)
        .title("Penalty Strokes")
        .cancelable(true)
        .items(penaltySelections)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override
          public void onSelection(final MaterialDialog dialog, final View view,
                                  final int which, final CharSequence text) {
            updateScore(round.holeScore(holeNumber).penaltyStrokes(which));
          }
        }).show();
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
            PlayRoundActivity.this.finish();
          }
        })
        .show();
  }

  private void showRoundSettings() {

    MaterialDialog dialog = new MaterialDialog.Builder(this)
        .title("Round Settings")
        .customView(R.layout.dialog_round_settings, true)
        .positiveText(android.R.string.ok)
        .cancelable(false)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(final MaterialDialog dialog) {

            CourseRating selectedRating =
                round.course.ratings[ratingSpinner.getSelectedItemPosition()];

            HoleSet selectedHoleSet =
                HoleSet.available(round.course)[holeSetSpinner.getSelectedItemPosition()];

            final Round unhandicappedRound = Round.create(currentUser, round.course, selectedRating,
                round.time, officialCheckBox.isChecked(), 0, handicapOverrideCheckBox.isChecked(),
                handicapOverrideSpinner.getSelectedItemPosition(), round.holeScores(),
                selectedHoleSet);

            setRound(unhandicappedRound);

            fetchAutoHandicap(unhandicappedRound);

          }
        }).build();

    ratingSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.play_round_tee_spinner);
    holeSetSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.play_round_holes_spinner);
    pickDateButton = (Button) dialog.getCustomView().findViewById(R.id.pick_date_button);
    officialCheckBox =
        (CheckBox) dialog.getCustomView().findViewById(R.id.play_round_official_checkbox);
    handicapOverrideCheckBox =
        (CheckBox) dialog.getCustomView().findViewById(R.id.play_round_handicap_override_checkbox);
    handicapOverrideSpinner =
        (Spinner) dialog.getCustomView().findViewById(R.id.play_round_handicap_override_spinner);

    int ratingIx = -1;
    for (int ix = 0; ix < round.course.ratings.length; ix++) {
      if (round.course.ratings[ix].id == round.rating.id) {
        ratingIx = ix;
      }
    }

    ratingSpinner.setAdapter(teeAdapter());
    ratingSpinner.setSelection(ratingIx);
    holeSetSpinner.setAdapter(holeSetAdapter());
    holeSetSpinner.setSelection(Arrays.asList(HoleSet.available(round.course)).indexOf(round.holeSet()));
    officialCheckBox.setChecked(round.official);
    handicapOverrideCheckBox.setChecked(round.isHandicapOverridden);
    handicapOverrideSpinner.setAdapter(handicapAdapter());
    handicapOverrideSpinner.setSelection(round.handicapOverride);
    handicapOverrideSpinner.setVisibility(
        handicapOverrideCheckBox.isChecked() ? View.VISIBLE : View.INVISIBLE);

    pickDateButton.setText(new DateTime(round.time).toString("MMM dd yyyy @ hh:mma"));
    pickDateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        showDateSelection(new DateTime(round.time));
      }
    });

    handicapOverrideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        handicapOverrideSpinner.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
      }
    });

    dialog.show();
  }

  private ArrayAdapter<String> teeAdapter() {
    String[] teeNames = new String[round.course.ratings.length];
    for (int ix = 0; ix < round.course.ratings.length; ix++) {
      teeNames[ix] = round.course.ratings[ix].teeName;
    }

    return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teeNames);
  }

  private ArrayAdapter<String> holeSetAdapter() {
    HoleSet[] availableSets = HoleSet.available(round.course);
    String[] holeSetNames = new String[availableSets.length];
    for (int ix = 0; ix < availableSets.length; ix++) {
      holeSetNames[ix] = availableSets[ix].title;
    }

    return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, holeSetNames);
  }

  private ArrayAdapter<String> handicapAdapter() {
    String[] handicaps = new String[51];
    for (int ix = 0; ix < handicaps.length; ix++) {
      handicaps[ix] = String.valueOf(ix);
    }

    return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, handicaps);
  }

  private void showDateSelection(final DateTime initial) {
    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
        .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                       @Override
                       public void onDateSet(final CalendarDatePickerDialog calendarDatePickerDialog,
                                             final int year, final int monthOfYear, final int dayOfMonth) {
                         showTimeSelection(initial.withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth));
                       }
                     }, initial.getYear(), initial.getMonthOfYear() - 1,
            initial.getDayOfMonth());

    calendarDatePickerDialog.show(getSupportFragmentManager(), getClass().getSimpleName() + ".DatePicker");
  }

  private void showTimeSelection(final DateTime initial) {
    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
        .newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
                       @Override
                       public void onTimeSet(final RadialTimePickerDialog radialTimePickerDialog, final int hourOfDay, final int minuteOfHour) {
                         setRound(round.withTime(initial.withHourOfDay(hourOfDay).withMinuteOfHour(minuteOfHour).getMillis()));
                         pickDateButton.setText(new DateTime(round.time).toString("MMM dd yyyy @ hh:mma"));
                       }
                     }, initial.getHourOfDay(), initial.getMinuteOfHour(),
            DateFormat.is24HourFormat(this));

    timePickerDialog.show(getSupportFragmentManager(), getClass().getSimpleName() + ".TimePicker");
  }

}
