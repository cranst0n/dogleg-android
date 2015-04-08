package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.location.Location;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.HoleFeature;
import org.cranst0n.dogleg.android.utils.Units;

public class HoleFeatureItem extends RelativeLayout {

  private final HoleFeature holeFeature;

  private ImageView iconView;
  private TextView nameView;

  private TextView distanceView1Label;
  private TextView distanceView2Label;
  private TextView distanceView3Label;
  private TextView distanceView1;
  private TextView distanceView2;
  private TextView distanceView3;

  private TextView elevationDifferenceView;

  public HoleFeatureItem(final Context context, final HoleFeature holeFeature) {
    super(context);

    this.holeFeature = holeFeature;

    init();
  }

  private void init() {

    inflate(getContext(), R.layout.item_hole_feature, this);

    this.iconView = (ImageView) findViewById(R.id.hole_feature_image);
    this.nameView = (TextView) findViewById(R.id.feature_name);
    this.elevationDifferenceView = (TextView) findViewById(R.id.feature_elevation_difference);

    this.distanceView1Label = (TextView) findViewById(R.id.feature_distance_1_label);
    this.distanceView2Label = (TextView) findViewById(R.id.feature_distance_2_label);
    this.distanceView3Label = (TextView) findViewById(R.id.feature_distance_3_label);
    this.distanceView1 = (TextView) findViewById(R.id.feature_distance_1);
    this.distanceView2 = (TextView) findViewById(R.id.feature_distance_2);
    this.distanceView3 = (TextView) findViewById(R.id.feature_distance_3);

    iconView.setImageResource(featureIcon(holeFeature));
    nameView.setText(holeFeature.name);
  }

  @Override
  public boolean onTouchEvent(final MotionEvent event) {
    return true;
  }

  public void updateDistances(final Location location) {

    if (location != null) {

      distanceView1Label.setVisibility(holeFeature.coordinates.length >= 1 ? VISIBLE : GONE);
      distanceView2Label.setVisibility(holeFeature.coordinates.length >= 2 ? VISIBLE : GONE);
      distanceView3Label.setVisibility(holeFeature.coordinates.length >= 3 ? VISIBLE : GONE);
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
