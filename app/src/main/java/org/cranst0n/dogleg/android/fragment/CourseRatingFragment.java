package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.utils.Json;

public class CourseRatingFragment extends BaseFragment {

  private CourseRating rating;

  private View ratingView;
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
  private TableLayout front9Table;
  private TableLayout back9Table;
  private TextView front9ParText;
  private TextView front9YardageText;
  private TextView back9ParText;
  private TextView back9YardageText;

  private final HoleViewHolder[] holeFieldViews = new HoleViewHolder[18];

  public CourseRatingFragment() {

  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    ratingView = inflater.inflate(R.layout.fragment_rating_info, container, false);

    parText = (TextView) ratingView.findViewById(R.id.par_text);
    yardageText = (TextView) ratingView.findViewById(R.id.yardage_text);
    ratingText = (TextView) ratingView.findViewById(R.id.rating_text);
    slopeText = (TextView) ratingView.findViewById(R.id.slope_text);
    frontRatingText = (TextView) ratingView.findViewById(R.id.front_rating_text);
    frontSlopeText = (TextView) ratingView.findViewById(R.id.front_slope_text);
    backRatingText = (TextView) ratingView.findViewById(R.id.back_rating_text);
    backSlopeText = (TextView) ratingView.findViewById(R.id.back_slope_text);
    bogeyRatingText = (TextView) ratingView.findViewById(R.id.bogey_rating_text);
    genderText = (TextView) ratingView.findViewById(R.id.gender_text);

    front9Table = (TableLayout) ratingView.findViewById(R.id.front_9_ratings_table);
    back9Table = (TableLayout) ratingView.findViewById(R.id.back_9_ratings_table);

    front9ParText = (TextView) ratingView.findViewById(R.id.front_9_par);
    front9YardageText = (TextView) ratingView.findViewById(R.id.front_9_yardage);
    back9ParText = (TextView) ratingView.findViewById(R.id.back_9_par);
    back9YardageText = (TextView) ratingView.findViewById(R.id.back_9_yardage);

    for (int ix = 0; ix < holeFieldViews.length; ix++) {
      holeFieldViews[ix] = new HoleViewHolder(ix + 1);
    }

    buildView();

    return ratingView;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      rating = Json.pimpedGson().fromJson(
          savedInstanceState.getString(CourseRating.class.getCanonicalName()), CourseRating.class);
    }
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(CourseRating.class.getCanonicalName(), Json.pimpedGson().toJson(rating));
  }

  public void setRating(final CourseRating rating) {
    this.rating = rating;
  }

  private void buildView() {
    if (rating != null) {

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

      if (rating.holeRatings().length == 9) {
        back9Table.setVisibility(View.GONE);
      }

      for (int ix = 0; ix < rating.holeRatings().length; ix++) {
        holeFieldViews[ix].par.setText("" + rating.holeRating(ix + 1).par);
        holeFieldViews[ix].yardage.setText("" + rating.holeRating(ix + 1).yardage);
        holeFieldViews[ix].handicap.setText("" + rating.holeRating(ix + 1).handicap);
      }

      front9ParText.setText(String.valueOf(rating.frontPar()));
      front9YardageText.setText(String.valueOf(rating.frontYardage()));
      back9ParText.setText(String.valueOf(rating.backPar()));
      back9YardageText.setText(String.valueOf(rating.backYardage()));
    }
  }

  private class HoleViewHolder {

    public final TextView number;
    public final TextView par;
    public final TextView yardage;
    public final TextView handicap;
    public final View divider;

    private HoleViewHolder(final int holeNumber) {

      number = (TextView) holeView(holeNumber, "number");
      par = (TextView) holeView(holeNumber, "par");
      yardage = (TextView) holeView(holeNumber, "yardage");
      handicap = (TextView) holeView(holeNumber, "handicap");
      divider = holeView(holeNumber, "divider");
    }

    private View holeView(final int holeNum, final String fieldSuffix) {
      String s = String.format("hole_%d_%s", holeNum, fieldSuffix);
      return ratingView.findViewById(
          getResources().getIdentifier(s, "id", activity.getPackageName()));
    }
  }
}
