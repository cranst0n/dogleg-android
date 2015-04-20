package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.PreferencesEditor;

public abstract class BackendComponent {

  public static final String AuthTokenHeader = "X-XSRF-TOKEN";

  @NonNull
  protected final Context context;
  protected final String Tag = getClass().getSimpleName();

  protected final Bus bus;

  protected BackendComponent(@NonNull final Context context) {
    this.context = context;
    this.bus = BusProvider.Instance.bus;
  }

  @NonNull
  protected final String serverUrl(@NonNull final String relativeUrl) {
    return serverUrl() + relativeUrl;
  }

  @NonNull
  public final String serverUrl() {
    String url = PreferencesEditor.getStringPreference(context, R.string.dogleg_server_url_key, "");
    return url == null ? "" : url;
  }

  @NonNull
  protected final String authToken() {
    String token = PreferencesEditor.getStringPreference(AuthTokenHeader, "");
    return token == null ? "" : token;
  }

  @NonNull
  protected final String saveAuthToken(@NonNull final String token) {
    PreferencesEditor.savePreference(AuthTokenHeader, token);
    return token;
  }

  protected final void clearAuthToken() {
    PreferencesEditor.removePreference(AuthTokenHeader);
  }

}
