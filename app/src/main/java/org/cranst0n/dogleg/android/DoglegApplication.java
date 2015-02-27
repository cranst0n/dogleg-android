package org.cranst0n.dogleg.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.PreferencesEditor;

public class DoglegApplication extends Application {

  private static DoglegApplication mDoglegApplication;
  private static Activity mCurrentActivity;
  private static User appUser;

  @Override
  public void onCreate() {

    super.onCreate();

    mDoglegApplication = this;
    BusProvider.instance().register(this);

    String userJson = PreferencesEditor.getStringPreference(this, R.string.app_user_key, "");

    if (!userJson.isEmpty()) {
      Gson gson = new GsonBuilder().create();
      appUser = gson.fromJson(userJson, User.class);

      // Make sure auth token is still valid
      new Authentication(this).authUser();
    } else {
      appUser = User.NO_USER;
    }

    BusProvider.instance().post(appUser);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();

    BusProvider.instance().unregister(this);
  }

  public static DoglegApplication application() {
    return mDoglegApplication;
  }

  public Activity currentActivity() {
    return mCurrentActivity;
  }

  public void setCurrentActivity(final Activity mCurrentActivity) {
    this.mCurrentActivity = mCurrentActivity;
  }

  public static Context context() {
    return mDoglegApplication.getApplicationContext();
  }

  public static Location lastKnownLocation() {

    LocationManager locationManager =
        (LocationManager)DoglegApplication.application().getSystemService(Context.LOCATION_SERVICE);

    Location networkLocation =
        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    Location gpsLocation =
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    if(gpsLocation == null) {
      return networkLocation;
    } else if(networkLocation == null) {
      return gpsLocation;
    } else if(gpsLocation.getAccuracy() < networkLocation.getAccuracy()) {
      return gpsLocation;
    } else {
      return networkLocation;
    }
  }

  @Produce
  public static User appUser() {
    return appUser;
  }

  @Subscribe
  public void newUser(final User user) {
    appUser = user;

    if (!appUser.equals(User.NO_USER)) {
      PreferencesEditor.savePreference(this, R.string.app_user_key, new GsonBuilder().create().toJson(appUser));
    } else {
      PreferencesEditor.removePreference(this, R.string.app_user_key);
    }
  }

}
