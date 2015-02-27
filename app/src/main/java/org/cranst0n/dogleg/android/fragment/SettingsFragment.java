package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.cranst0n.dogleg.android.R;

public class SettingsFragment extends PreferenceFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    getActivity().setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
    addPreferencesFromResource(R.xml.preferences);
  }
}
