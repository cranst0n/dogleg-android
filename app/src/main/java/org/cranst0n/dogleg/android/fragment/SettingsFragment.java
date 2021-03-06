package org.cranst0n.dogleg.android.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.cranst0n.dogleg.android.BuildConfig;
import org.cranst0n.dogleg.android.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    View v = super.onCreateView(inflater, container, savedInstanceState);

    if (v != null) {

      ListView lv = (ListView) v.findViewById(android.R.id.list);

      lv.setPadding(lv.getPaddingLeft(), 20,
          lv.getPaddingRight(), lv.getPaddingBottom());
    }

    try {

      String appVersion = getActivity().getPackageManager().
          getPackageInfo(getActivity().getPackageName(), 0).versionName;

      if (BuildConfig.DEBUG) {
        try {

          String[] versionComponents = appVersion.split("\\.");

          if (versionComponents.length >= 2) {
            int major = Integer.valueOf(versionComponents[0]);
            int minor = Integer.valueOf(versionComponents[1]);
            appVersion = String.format("%d.%d-SNAPSHOT", major, (minor + 1));
          } else {
            double doubleVersion = Double.parseDouble(appVersion);
            appVersion = String.valueOf(doubleVersion + 0.1) + "-SNAPSHOT";
          }
        } catch (final NumberFormatException e) {
          appVersion += "+-SNAPSHOT";
        }
      }

      findPreference("version").setSummary(appVersion);
    } catch (PackageManager.NameNotFoundException e) {
      findPreference("version").setSummary("Unknown");
    }

    return v;
  }
}
