package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.location.Location;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.model.HoleFeature;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.utils.Units;

import java.util.Arrays;

public class HoleFeatureItem extends RelativeLayout {

  private final HoleFeature holeFeature;

  private ImageView iconView;
  private TextView nameView;
  private TextView distancesView;
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
    this.distancesView = (TextView) findViewById(R.id.feature_distances);
    this.elevationDifferenceView = (TextView) findViewById(R.id.feature_elevation_difference);

    iconView.setImageResource(featureIcon(holeFeature));
    nameView.setText(holeFeature.name);

    distancesView.setText("---");
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return true;
  }

  public void updateDistances(final Location location) {

    if (location != null) {

      StringBuilder sb = new StringBuilder();
      String[] labels;

      if (holeFeature.coordinates.length == 1) {
        labels = new String[]{"Reach"};
      } else if (holeFeature.coordinates.length == 2) {
        labels = new String[]{"Reach", "Carry"};
      } else if (holeFeature.coordinates.length == 3) {
        labels = new String[]{"Front", "Middle", "Back"};
      } else {
        labels = new String[holeFeature.coordinates.length];
        Arrays.fill(labels, "");
      }

      for (int jx = 0; jx < holeFeature.coordinates.length; jx++) {

        LatLon ll = holeFeature.coordinates[jx];
        double meters = ll.toLocation().distanceTo(location);
        int yards = (int) Math.round(Units.metersToYards(meters));

        sb.append(String.format(" %s: %d", labels[jx], yards));
      }

      distancesView.setText(sb.toString());

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
