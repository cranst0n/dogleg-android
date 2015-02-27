package org.cranst0n.dogleg.android.backend;

import android.content.Context;

import com.squareup.otto.Bus;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.PreferencesEditor;

public abstract class BackendComponent {

  public static final String AuthTokenHeader = "X-XSRF-TOKEN";

  protected final Context context;
  protected final String Tag = getClass().getSimpleName();

  protected final Bus bus;

  protected BackendComponent(final Context context) {
    this.context = context;
    this.bus = BusProvider.Instance.bus;
  }

  protected final String serverUrl(final String relativeUrl) {
    return serverUrl() + relativeUrl;
  }

  public final String serverUrl() {
    return PreferencesEditor.getStringPreference(context, R.string.dogleg_server_url_key, "");
  }

  protected final String authToken() {
    return PreferencesEditor.getStringPreference(AuthTokenHeader, "");
  }

  protected final String saveAuthToken(final String token) {
    PreferencesEditor.savePreference(AuthTokenHeader, token);
    return token;
  }

  protected final void clearAuthToken() {
    PreferencesEditor.removePreference(AuthTokenHeader);
  }

}
