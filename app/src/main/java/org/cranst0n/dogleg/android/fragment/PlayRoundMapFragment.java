package org.cranst0n.dogleg.android.fragment;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Bus;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.PlayRoundActivity;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.Hole;
import org.cranst0n.dogleg.android.model.HoleFeature;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.utils.Units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayRoundMapFragment extends BaseFragment implements GoogleMap.OnMarkerDragListener {

  private Bus bus;

  private View mapFragmentView;
  private ImageButton resetCameraButton;
  private ImageButton layerToggleButton;
  private ImageButton holeFlyByButton;

  private MapView mapView;
  private GoogleMap map;

  private boolean layersVisible = true;
  private final List<Marker> featureDistanceMarkers = new ArrayList<>();
  private Marker userDistanceMarker;
  private final List<Circle> standardDistanceCircles = new ArrayList<>();
  private boolean mapInitialized;

  private IconGenerator holeFeatureIconFactory;
  private IconGenerator userDistanceIconFactory;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    holeFeatureIconFactory = new IconGenerator(context);
    userDistanceIconFactory = new IconGenerator(context);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    mapFragmentView = inflater.inflate(R.layout.fragment_play_round_map, container, false);

    // Gets the MapView from the XML layout and creates it
    mapView = (MapView) mapFragmentView.findViewById(R.id.round_map_view);
    resetCameraButton = (ImageButton) mapFragmentView.findViewById(R.id.reset_camera_button);
    layerToggleButton = (ImageButton) mapFragmentView.findViewById(R.id.layer_toggle_button);
    holeFlyByButton = (ImageButton) mapFragmentView.findViewById(R.id.hole_flyby_button);

    mapView.onCreate(savedInstanceState);

    // Gets to GoogleMap from the MapView and does initialization stuff
    map = mapView.getMap();
    map.setOnMarkerDragListener(this);
    map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
      @Override
      public void onMapClick(final LatLng latLng) {
        userDistanceMarker.setPosition(latLng);
        updateDistanceMarker(userDistanceMarker, referenceLocation(), userDistanceIconFactory);
        userDistanceMarker.setVisible(true);
      }
    });

    if (map != null) {

      map.getUiSettings().setCompassEnabled(false);
      map.getUiSettings().setMyLocationButtonEnabled(false);

      map.setMyLocationEnabled(true);
      map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
          return true;
        }
      });

      // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
      MapsInitializer.initialize(getActivity());
      mapInitialized = true;

    } else {
      SnackBars.showSimple(activity, "Map view is unavailable.");
    }

    resetCameraButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        cameraToOverheadView(false);
      }
    });

    layerToggleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        setLayersVisible(!layersVisible);

        if (layersVisible) {
          layerToggleButton.setImageResource(R.drawable.ic_maps_layers);
        } else {
          layerToggleButton.setImageResource(R.drawable.ic_maps_layers_clear);
        }
      }
    });

    holeFlyByButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        holeFlyby();
      }
    });

    View mapDistanceMarker = inflater.inflate(R.layout.map_distance_marker, null);
    holeFeatureIconFactory.setBackground(null);
    holeFeatureIconFactory.setContentView(mapDistanceMarker);

    View mapDraggableDistanceMarker = inflater.inflate(R.layout.map_user_distance_marker, null);
    userDistanceIconFactory.setBackground(null);
    userDistanceIconFactory.setContentView(mapDraggableDistanceMarker);

    return mapFragmentView;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
    mapView.onDestroy();
  }

  @Override
  public void onResume() {
    mapView.onResume();
    super.onResume();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  public void onMarkerDragEnd(final Marker marker) {
    updateUserDistanceMarker(referenceLocation());
  }

  @Override
  public void onMarkerDragStart(final Marker marker) {

  }

  @Override
  public void onMarkerDrag(final Marker marker) {

  }

  private Hole currentHole() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).currentHole();
    }

    return null;
  }

  private HoleRating currentHoleRating() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).currentHoleRating();
    }

    return null;
  }

  private Location lastKnownLocation() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).lastKnownLocation();
    }

    return null;
  }

  private Location referenceLocation() {
    return lastKnownLocation() == null ?
        currentHole().teeFeature().center().toLocation() : lastKnownLocation();
  }

  public void locationUpdated(final Location location) {
    updateFeatureDistanceMarkers(location);
    updateUserDistanceMarker(location);
  }

  public void holeUpdated(final int currentHole) {

    if (mapInitialized) {

      final Hole hole = currentHole();

      if (hole.hasFlybyPath()) {
        holeFlyByButton.setImageResource(R.drawable.ic_device_airplanemode_on);
      } else {
        holeFlyByButton.setImageResource(R.drawable.ic_device_airplanemode_off);
      }

      if (hole != null && hole.features.length > 1) {
        featureDistanceMarkers.clear();
        standardDistanceCircles.clear();
        cameraToOverheadView(true);
      }
    }
  }

  private void cameraToOverheadView(final boolean initial) {

    final Hole hole = currentHole();
    final HoleRating holeRating = currentHoleRating();

    if (initial) {
      map.clear();
    }

    LatLngBounds.Builder b = LatLngBounds.builder();

    for (HoleFeature f : hole.features) {
      for (LatLon fl : f.coordinates) {
        b.include(fl.toLatLng());
      }
    }

    double bearing = hole.teeFeature().center().toLocation().
        bearingTo(hole.greenFeature().center().toLocation());
    float zoom = overviewZoomLevel(hole);

    CameraPosition cp = new CameraPosition.Builder()
        .target(b.build().getCenter())
        .zoom(zoom)
        .bearing((float) bearing)
        .tilt(0)
        .build();

    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp), new GoogleMap.CancelableCallback() {
      @Override
      public void onFinish() {
        if (initial) {
          addHoleMarkers(hole, holeRating, referenceLocation());
        }
      }

      @Override
      public void onCancel() {

      }
    });
  }

  private float overviewZoomLevel(final Hole hole) {

    LatLngBounds.Builder b = LatLngBounds.builder();

    for (HoleFeature f : hole.features) {
      for (LatLon fl : f.coordinates) {
        b.include(fl.toLatLng());
      }
    }

    LatLngBounds holeBounds = b.build();
    double radius = LatLon.fromLatLng(holeBounds.northeast).distanceTo(holeBounds.southwest) / 2;

    double scale = radius / 500;
    return (float) (16 - Math.log(scale) / Math.log(2)) - 0.75f;
  }

  private void holeFlyby() {
    final Hole hole = currentHole();

    if (hole.hasFlybyPath()) {
      if (hole.flybyPathFeature().coordinates.length > 1) {
        HoleFeature flybyFeature = hole.flybyPathFeature();
        flybyStep(Arrays.asList(flybyFeature.coordinates), 0);
      }
    }
  }

  private void flybyStep(final List<LatLon> flybyPath, final int step) {

    if (step < flybyPath.size()) {

      LatLon nextPoint = flybyPath.get(step);

      double bearing;
      int animationDuration;

      if (step == 0) {
        bearing = flybyPath.get(0).toLocation().bearingTo(flybyPath.get(1).toLocation());
        animationDuration = 2000;
      } else {
        Location cameraLocation = new Location("cameraLocation");
        cameraLocation.setLatitude(map.getCameraPosition().target.latitude);
        cameraLocation.setLongitude(map.getCameraPosition().target.longitude);
        bearing = cameraLocation.bearingTo(nextPoint.toLocation());
        animationDuration =
            (int) (cameraLocation.distanceTo(nextPoint.toLocation()) *
                Math.min(20 * (step / 2d), 750));
      }

      CameraPosition cp = new CameraPosition.Builder()
          .target(nextPoint.toLatLng())
          .bearing((float) bearing)
          .tilt(70)
          .zoom(19f)
          .build();

      map.animateCamera(CameraUpdateFactory.newCameraPosition(cp), animationDuration, new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
          flybyStep(flybyPath, step + 1);
        }

        @Override
        public void onCancel() {

        }
      });
    } else {

      // Fly behind green facing tee, then back to overhead view
      double bearing =
          flybyPath.get(flybyPath.size() - 1).toLocation().bearingTo(flybyPath.get(0).toLocation());
      int animationDuration = 2000;

      CameraPosition cp = new CameraPosition.Builder()
          .target(map.getCameraPosition().target)
          .bearing((float) bearing)
          .tilt(65)
          .zoom(18f)
          .build();

      map.animateCamera(CameraUpdateFactory.newCameraPosition(cp), animationDuration, new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              cameraToOverheadView(false);
            }
          }, 750);
        }

        @Override
        public void onCancel() {

        }
      });
    }
  }

  private void setLayersVisible(final boolean visible) {

    layersVisible = visible;

    for (Marker marker : featureDistanceMarkers) {
      marker.setVisible(visible);
    }

    userDistanceMarker.setVisible(visible);

    for (Circle standardDistanceCircle : standardDistanceCircles) {
      standardDistanceCircle.setVisible(visible);
    }
  }

  private void addHoleMarkers(final Hole hole, final HoleRating holeRating,
                              final Location reference) {

    if (reference != null) {

      if (userDistanceMarker != null) {
        userDistanceMarker.remove();
      }

      userDistanceMarker =
          map.addMarker(new MarkerOptions().
              icon(BitmapDescriptorFactory.fromBitmap(userDistanceIconFactory.makeIcon("Drag"))).
              position(hole.teeFeature().center().toLatLng()).
              draggable(true).
              visible(false).
              anchor(0.5f, 1f));

      for (HoleFeature feature : hole.displayableFeatures()) {

        for (LatLon latLon : feature.coordinates) {

          String distance = String.format("%d",
              (int) Math.round(Units.metersToYards(latLon.distanceTo(reference))));

          MarkerOptions markerOptions = new MarkerOptions().
              icon(BitmapDescriptorFactory.fromBitmap(holeFeatureIconFactory.makeIcon(distance))).
              position(latLon.toLatLng()).
              anchor(0.5f, 1f);

          featureDistanceMarkers.add(map.addMarker(markerOptions));
        }
      }

      if (holeRating.par > 3) {
        LatLng greenCenter =
            new LatLng(hole.greenFeature().center().latitude, hole.greenFeature().center().longitude);

        standardDistanceCircles.add(map.addCircle(createYardageCircle(greenCenter, 100, Color.RED)));
        standardDistanceCircles.add(map.addCircle(createYardageCircle(greenCenter, 150, Color.WHITE)));
        standardDistanceCircles.add(map.addCircle(createYardageCircle(greenCenter, 200, Color.BLUE)));
      }
    }
  }

  private CircleOptions createYardageCircle(final LatLng center, final int yards, final int color) {
    return new CircleOptions()
        .center(center)
        .radius(Units.yardsToMeters(yards))
        .fillColor(Color.TRANSPARENT)
        .strokeColor(color)
        .strokeWidth(3);
  }

  private void updateFeatureDistanceMarkers(final Location location) {
    for (Marker featureMarker : featureDistanceMarkers) {
      updateDistanceMarker(featureMarker, location, holeFeatureIconFactory);
    }
  }

  private void updateUserDistanceMarker(final Location location) {
    if (userDistanceMarker != null) {
      updateDistanceMarker(userDistanceMarker, location, userDistanceIconFactory);
    }
  }

  private void updateDistanceMarker(final Marker marker, final Location toLocation, final IconGenerator iconGenerator) {
    Location markerLocation = markerLocation(marker);
    int distance =
        (int) Math.round(Units.metersToYards(toLocation.distanceTo(markerLocation)));

    try {
      marker.setIcon(
          BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(String.valueOf(distance))));
    } catch (Exception ex) {
      Log.e(getClass().getSimpleName(), "Marker ex: " + ex.getMessage(), ex);
    }
  }

  private Location markerLocation(final Marker marker) {
    Location markerLocation = new Location("userMarker");
    markerLocation.setLatitude(marker.getPosition().latitude);
    markerLocation.setLongitude(marker.getPosition().longitude);

    return markerLocation;
  }

}
