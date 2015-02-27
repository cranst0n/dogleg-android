package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.model.HoleRating;

import java.util.Arrays;
import java.util.Comparator;

public class CourseRatingFragment extends BaseFragment {

  private CourseRating rating;

  private TextView parText;
  private TextView yardageText;
  private TextView ratingText;
  private TextView slopeText;
  private TextView frontRatingText;
  private TextView frontSlopeText;
  private TextView backRatingText;
  private TextView backSlopeText;
  private TextView bogeyRatingText;
  private TextView genderText;
  private TableLayout holeRatingsTable;

  public CourseRatingFragment() {

  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.fragment_rating_info, container, false);

    parText = (TextView) v.findViewById(R.id.par_text);
    yardageText = (TextView) v.findViewById(R.id.yardage_text);
    ratingText = (TextView) v.findViewById(R.id.rating_text);
    slopeText = (TextView) v.findViewById(R.id.slope_text);
    frontRatingText = (TextView) v.findViewById(R.id.front_rating_text);
    frontSlopeText = (TextView) v.findViewById(R.id.front_slope_text);
    backRatingText = (TextView) v.findViewById(R.id.back_rating_text);
    backSlopeText = (TextView) v.findViewById(R.id.back_slope_text);
    bogeyRatingText = (TextView) v.findViewById(R.id.bogey_rating_text);
    genderText = (TextView) v.findViewById(R.id.gender_text);

    holeRatingsTable = (TableLayout) v.findViewById(R.id.hole_ratings_row);

    buildView();

    return v;
  }

  public void setRating(final CourseRating rating) {
    this.rating = rating;
  }

  private void buildView() {
    if(rating != null) {
      parText.setText(String.valueOf(rating.par()));
      yardageText.setText(String.valueOf(rating.yardage()));
      ratingText.setText(String.format("%.1f", rating.rating));
      slopeText.setText(String.format("%.1f", rating.slope));
      frontRatingText.setText(String.format("%.1f", rating.frontRating));
      frontSlopeText.setText(String.format("%.1f", rating.frontSlope));
      backRatingText.setText(String.format("%.1f", rating.backRating));
      backSlopeText.setText(String.format("%.1f", rating.backSlope));
      bogeyRatingText.setText(String.format("%.1f", rating.bogeyRating));
      genderText.setText(rating.gender.toString());

      Arrays.sort(rating.holeRatings, new Comparator<HoleRating>() {
        @Override
        public int compare(HoleRating lhs, HoleRating rhs) {
          return lhs.number - rhs.number;
        }
      });

      for (int ix = 0; ix < rating.holeRatings.length; ix++) {
        holeRatingsTable.addView(divider());
        holeRatingsTable.addView(holeView(ix));
      }
    }
  }

  private View holeView(final int holeIx) {
    TableRow tr = new TableRow(context);
    tr.setPadding(4, 18, 4, 18);

    TextView holeNumText =
        holeTextView(String.format("%d", rating.holeRatings[holeIx].number), 0, 0);
    TextView holeParText =
        holeTextView(String.format("%d", rating.holeRatings[holeIx].par), 0, 1);
    TextView holeYardageText =
        holeTextView(String.format("%d", rating.holeRatings[holeIx].yardage), 0, 2);
    TextView holeHandicapText =
        holeTextView(String.format("%d", rating.holeRatings[holeIx].handicap), 0, 3);

    tr.addView(holeNumText);
    tr.addView(holeParText);
    tr.addView(holeYardageText);
    tr.addView(holeHandicapText);

    return tr;
  }

  private TextView holeTextView(final String text, final int row, final int column) {
    TextView textView = new TextView(context);
    textView.setTextSize(12f);
    textView.setTextColor(getResources().getColor(R.color.text_grey));
    textView.setText(text);
    textView.setGravity(Gravity.CENTER);
    textView.setWidth(0);

    return textView;
  }

  private View divider() {
    View v = new View(context);
    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        1);
    v.setLayoutParams(lp);
    v.setBackgroundColor(getResources().getColor(R.color.text_grey));
    return v;
  }
}
