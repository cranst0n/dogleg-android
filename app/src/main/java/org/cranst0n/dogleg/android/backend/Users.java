package org.cranst0n.dogleg.android.backend;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Crypto;

public class Users extends BackendComponent {

  private static final String AVATAR_URL = "/image/avatar/%d?width=%d&height=%d";
  private static final String CHANGE_PASSWORD_URL = "/users/%d/password";

  public Users(final Context context) {
    super(context);
  }

  public String avatarUrl(final User user) {
    return serverUrl(String.format(AVATAR_URL, user.id, 128, 128));
  }

  public BackendResponse<JsonObject, User> changePassword(final User user,
                                                          final String oldPassword,
                                                          final String newPassword,
                                                          final String newPasswordConfirm) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("oldPassword", Crypto.hashPassword(oldPassword));
    jsonObject.addProperty("newPassword", Crypto.hashPassword(newPassword));
    jsonObject.addProperty("newPasswordConfirm", Crypto.hashPassword(newPasswordConfirm));

    return new BackendResponse<>(Ion.with(context)
        .load("PUT", serverUrl(String.format(CHANGE_PASSWORD_URL, user.id)))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(jsonObject)
        .asJsonObject()
        .withResponse(), User.class);
  }

}
