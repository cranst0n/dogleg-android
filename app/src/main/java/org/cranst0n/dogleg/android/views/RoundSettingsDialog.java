package org.cranst0n.dogleg.android.views;

import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.joda.time.DateTime;

import java.util.Arrays;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class RoundSettingsDialog {

  private Round round;
  private final FragmentActivity activity;
  private final RoundSettingsCallback callback;

  private final Rounds rounds;

  private Spinner ratingSpinner;
  private Spinner holeSetSpinner;
  private Button pickDateButton;
  private CheckBox officialCheckBox;

  private GridLayout handicapLayout;
  private RadioButton autoHandicapButton;
  private CircularProgressBar fetchingAutoHandicapIndicator;
  private TextView autoHandicapText;
  private RadioButton overrideHandicapButton;
  private Spinner overrideHandicapSpinner;

  public interface RoundSettingsCallback {
    void settingsUpdated(final Round round);
  }

  public RoundSettingsDialog(final Round round, final FragmentActivity activity,
                             final RoundSettingsCallback callback) {

    this.round = round;
    this.activity = activity;
    this.callback = callback;

    this.rounds = new Rounds(activity);
  }

  public void show() {

    MaterialDialog dialog = new MaterialDialog.Builder(activity)
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

            final Round unhandicappedRound = Round.create(round.id, round.user, round.course,
                selectedRating, round.time, officialCheckBox.isChecked(), 0,
                overrideHandicapButton.isChecked(),
                overrideHandicapSpinner.getSelectedItemPosition(), round.holeScores(),
                selectedHoleSet);

            if (unhandicappedRound.isHandicapOverridden) {
              callback.settingsUpdated(unhandicappedRound);
            } else {
              rounds.handicapRound(round.roundSlope(), round.numHoles(), round.time)
                  .onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
                    @Override
                    public void onSuccess(final RoundHandicapResponse value) {
                      callback.settingsUpdated(round.withAutoHandicap(value.handicap));
                    }
                  });
            }

          }
        }).build();

    ratingSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.play_round_tee_spinner);
    holeSetSpinner = (Spinner) dialog.getCustomView().findViewById(R.id.play_round_holes_spinner);
    pickDateButton = (Button) dialog.getCustomView().findViewById(R.id.pick_date_button);
    officialCheckBox =
        (CheckBox) dialog.getCustomView().findViewById(R.id.play_round_official_checkbox);
    handicapLayout = (GridLayout) dialog.getCustomView().findViewById(R.id.handicap_layout);
    autoHandicapButton = (RadioButton) dialog.getCustomView().findViewById(R.id.auto_handicap_button);
    fetchingAutoHandicapIndicator =
        (CircularProgressBar) dialog.getCustomView().findViewById(R.id.fetching_auto_handicap_indicator);
    autoHandicapText = (TextView) dialog.getCustomView().findViewById(R.id.auto_handicap_text);
    overrideHandicapButton =
        (RadioButton) dialog.getCustomView().findViewById(R.id.override_handicap_button);
    overrideHandicapSpinner =
        (Spinner) dialog.getCustomView().findViewById(R.id.override_handicap_spinner);

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

    overrideHandicapSpinner.setAdapter(handicapAdapter());
    overrideHandicapSpinner.setSelection(round.handicapOverride);
    overrideHandicapSpinner.setVisibility(
        overrideHandicapButton.isChecked() ? View.VISIBLE : View.INVISIBLE);

    ratingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateAutoHandicapValue();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    holeSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateAutoHandicapValue();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    pickDateButton.setText(new DateTime(round.time).toString("MMM dd yyyy @ hh:mma"));
    pickDateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        showDateSelection(new DateTime(round.time));
      }
    });

    officialCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        handicapLayout.setVisibility(b ? View.VISIBLE : View.GONE);
      }
    });

    autoHandicapButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        overrideHandicapButton.setChecked(!isChecked);
        autoHandicapText.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
      }
    });

    overrideHandicapButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        autoHandicapButton.setChecked(!isChecked);
        overrideHandicapSpinner.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
      }
    });

    autoHandicapButton.setChecked(!round.isHandicapOverridden);
    overrideHandicapButton.setChecked(round.isHandicapOverridden);

    dialog.show();
  }

  private void updateAutoHandicapValue() {

    CourseRating selectedRating =
        round.course.ratings[ratingSpinner.getSelectedItemPosition()];

    HoleSet selectedHoleSet =
        HoleSet.available(round.course)[holeSetSpinner.getSelectedItemPosition()];

    updateAutoHandicapValue(selectedRating.slope(selectedHoleSet),
        selectedHoleSet.numHoles, round.time);
  }

  private void updateAutoHandicapValue(final double slope, final int numHoles, final DateTime time) {

    if (autoHandicapButton.isChecked()) {
      autoHandicapText.setVisibility(View.GONE);
      fetchingAutoHandicapIndicator.setVisibility(View.VISIBLE);
    }

    rounds.handicapRound(slope, numHoles, time).
        onSuccess(new BackendResponse.BackendSuccessListener<RoundHandicapResponse>() {
          @Override
          public void onSuccess(final RoundHandicapResponse value) {
            autoHandicapText.setText(String.valueOf(value.handicap));
          }
        }).
        onFinally(new BackendResponse.BackendFinallyListener() {
          @Override
          public void onFinally() {
            if (autoHandicapButton.isChecked()) {
              autoHandicapText.setVisibility(View.VISIBLE);
              fetchingAutoHandicapIndicator.setVisibility(View.GONE);
            }
          }
        });
  }

  private ArrayAdapter<String> teeAdapter() {
    String[] teeNames = new String[round.course.ratings.length];
    for (int ix = 0; ix < round.course.ratings.length; ix++) {
      teeNames[ix] = round.course.ratings[ix].teeName;
    }

    return new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, teeNames);
  }

  private ArrayAdapter<String> holeSetAdapter() {
    HoleSet[] availableSets = HoleSet.available(round.course);
    String[] holeSetNames = new String[availableSets.length];
    for (int ix = 0; ix < availableSets.length; ix++) {
      holeSetNames[ix] = availableSets[ix].title;
    }

    return new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, holeSetNames);
  }

  private ArrayAdapter<String> handicapAdapter() {
    String[] handicaps = new String[51];
    for (int ix = 0; ix < handicaps.length; ix++) {
      handicaps[ix] = String.valueOf(ix);
    }

    return new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, handicaps);
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

    calendarDatePickerDialog.show(activity.getSupportFragmentManager(), getClass().getSimpleName() + ".DatePicker");
  }

  private void showTimeSelection(final DateTime initial) {
    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
        .newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
                       @Override
                       public void onTimeSet(final RadialTimePickerDialog radialTimePickerDialog, final int hourOfDay, final int minuteOfHour) {
                         round = round.withTime(initial.withHourOfDay(hourOfDay).withMinuteOfHour(minuteOfHour));
                         pickDateButton.setText(new DateTime(round.time).toString("MMM dd yyyy @ hh:mma"));
                         updateAutoHandicapValue();
                       }
                     }, initial.getHourOfDay(), initial.getMinuteOfHour(),
            DateFormat.is24HourFormat(activity));

    timePickerDialog.show(activity.getSupportFragmentManager(), getClass().getSimpleName() + ".TimePicker");
  }

}
