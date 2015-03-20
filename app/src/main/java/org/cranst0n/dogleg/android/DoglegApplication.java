package org.cranst0n.dogleg.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSender;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.PreferencesEditor;

import java.util.concurrent.atomic.AtomicReference;

@ReportsCrashes
public class DoglegApplication extends Application {

  private static DoglegApplication doglegApplication;
  private static final AtomicReference<Activity> currentActivity = new AtomicReference<>(null);
  private static User appUser;

  private static GoogleApiClient googleApiClient;

  @Override
  public void onCreate() {

    doglegApplication = this;
    googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
    googleApiClient.connect();

    initARCA();

    super.onCreate();

    BusProvider.Instance.bus.register(this);

    String userJson = PreferencesEditor.getStringPreference(this, R.string.app_user_key, "");

    if (!userJson.isEmpty()) {
      Gson gson = new GsonBuilder().create();
      appUser = gson.fromJson(userJson, User.class);

      // Make sure auth token is still valid
      new Authentication(this).authUser();
    } else {
      appUser = User.NO_USER;
    }

    BusProvider.Instance.bus.post(appUser);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();

    BusProvider.Instance.bus.unregister(this);
  }

  public static DoglegApplication application() {
    return doglegApplication;
  }

  public static Activity currentActivity() {
    return currentActivity.get();
  }

  public static void setCurrentActivity(final Activity activity) {
    currentActivity.set(activity);
  }

  public static Context context() {
    return doglegApplication.getApplicationContext();
  }

  public static GoogleApiClient googleApiClient() {
    return googleApiClient;
  }

  public FusedLocationProviderApi locationProviderApi() {
    return LocationServices.FusedLocationApi;
  }

  public Location lastKnownLocation() {
    return locationProviderApi().getLastLocation(googleApiClient);
  }

  @Produce
  public static User appUser() {
    return appUser;
  }

  @Subscribe
  public void newUser(final User user) {
    appUser = user;

    if (appUser.isValid()) {
      PreferencesEditor.savePreference(this, R.string.app_user_key, new GsonBuilder().create().toJson(appUser));
    } else {
      PreferencesEditor.removePreference(this, R.string.app_user_key);
    }
  }

  private void initARCA() {

    ACRA.init(this);

    initARCAHandler();

    PreferencesEditor.getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals(R.string.dogleg_server_url_key)) {
          initARCAHandler();
        }
      }
    });
  }

  private void initARCAHandler() {

    String httpReportUrl =
        PreferencesEditor.getStringPreference(this, R.string.dogleg_server_url_key, "");

    ReportSender reportSender =
        new HttpSender(HttpSender.Method.POST, HttpSender.Type.JSON, httpReportUrl + "/crashreport", null);
    ACRA.getErrorReporter().setReportSender(reportSender);
  }

}
