package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.FileUpload;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.model.UserStats;
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
  private static final String STATS_URL = "/users/stats/%d";

  public Users(@NonNull final Context context) {
    super(context);
  }

  @NonNull
  public String avatarUrl(@NonNull final User user) {
    return serverUrl(String.format(AVATAR_URL, user.id, 128, 128));
  }

  @NonNull
  public BackendResponse<JsonObject, User> create(@NonNull final User user) {
    return new BackendResponse<>(Ion.with(context)
        .load("POST", serverUrl(CREATE_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(Json.pimpedGson().toJsonTree(user).getAsJsonObject())
        .asJsonObject()
        .withResponse(), User.class);
  }

  @NonNull
  public BackendResponse<JsonObject, User> changeAvatar(@NonNull final User user,
                                                        @NonNull final Bitmap avatar) {

    FileUpload upload = new FileUpload(avatar);

    return new BackendResponse<>(Ion.with(context)
        .load("PUT", serverUrl(String.format(CHANGE_AVATAR_URL, user.id)))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(Json.pimpedGson().toJsonTree(upload).getAsJsonObject())
        .asJsonObject()
        .withResponse(), User.class);
  }

  @NonNull
  public BackendResponse<JsonObject, User> changePassword(@NonNull final User user,
                                                          @NonNull final String oldPassword,
                                                          @NonNull final String newPassword,
                                                          @NonNull final String newPasswordConfirm) {

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

  @NonNull
  public BackendResponse<JsonObject, User> resetPassword(@NonNull final User user,
                                                         @NonNull final String newPassword,
                                                         @NonNull final String newPasswordConfirm) {

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

  @NonNull
  public BackendResponse<JsonArray, List<User>> searchByName(@NonNull final String text) {
    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(String.format(SEARCH_BY_NAME_URL, text)))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonArray()
        .withResponse(), new TypeToken<List<User>>() {
    }.getType());
  }

  @NonNull
  public BackendResponse<JsonObject, UserStats> stats(final long userId) {
    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(String.format(STATS_URL, userId)))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), UserStats.class);
  }

}
