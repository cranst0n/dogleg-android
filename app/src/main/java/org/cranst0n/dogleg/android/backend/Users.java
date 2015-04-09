package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.FileUpload;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Crypto;
import org.cranst0n.dogleg.android.utils.Json;

import java.util.List;

public class Users extends BackendComponent {

  private static final String AVATAR_URL = "/users/avatar/%d?width=%d&height=%d";
  private static final String CREATE_URL = "/users";
  private static final String CHANGE_AVATAR_URL = "/users/%d/avatar";
  private static final String CHANGE_PASSWORD_URL = "/users/%d/password";
  private static final String RESET_PASSWORD_URL = "/users/%d/passwordReset";
  private static final String SEARCH_BY_NAME_URL = "/users/named/%s";

  public Users(final Context context) {
    super(context);
  }

  public String avatarUrl(final User user) {
    return serverUrl(String.format(AVATAR_URL, user.id, 128, 128));
  }

  public BackendResponse<JsonObject, User> create(final User user) {
    return new BackendResponse<>(Ion.with(context)
        .load("POST", serverUrl(CREATE_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(Json.pimpedGson().toJsonTree(user).getAsJsonObject())
        .asJsonObject()
        .withResponse(), User.class);
  }

  public BackendResponse<JsonObject, User> changeAvatar(final User user, final Bitmap avatar) {

    FileUpload upload = new FileUpload(avatar);

    return new BackendResponse<>(Ion.with(context)
        .load("PUT", serverUrl(String.format(CHANGE_AVATAR_URL, user.id)))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(Json.pimpedGson().toJsonTree(upload).getAsJsonObject())
        .asJsonObject()
        .withResponse(), User.class);
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

  public BackendResponse<JsonObject, User> resetPassword(final User user, final String
      newPassword, final String newPasswordConfirm) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("newPassword", Crypto.hashPassword(newPassword));
    jsonObject.addProperty("newPasswordConfirm", Crypto.hashPassword(newPasswordConfirm));

    return new BackendResponse<>(Ion.with(context)
        .load("PUT", serverUrl(String.format(RESET_PASSWORD_URL, user.id)))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(jsonObject)
        .asJsonObject()
        .withResponse(), User.class);
  }

  public BackendResponse<JsonArray, List<User>> searchByName(final String text) {
    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(String.format(SEARCH_BY_NAME_URL, text)))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonArray()
        .withResponse(), new TypeToken<List<User>>() {
    }.getType());
  }

}
