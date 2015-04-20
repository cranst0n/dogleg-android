package org.cranst0n.dogleg.android.backend;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.model.AuthToken;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Crypto;
import org.cranst0n.dogleg.android.utils.SnackBars;

import java.net.HttpURLConnection;

public class Authentication extends BackendComponent {

  private static final String LOGIN_URL = "/login";
  private static final String LOGOUT_URL = "/logout";
  private static final String AUTH_USER_URL = "/authuser";
  private static final String AUTH_ADMIN_URL = "/authadmin";

  public Authentication(@NonNull final Context context) {
    super(context);
  }

  @NonNull
  public BackendResponse<JsonObject, AuthToken> login(@NonNull final String username,
                                                      @NonNull final String password) {

    String hashedPassword = Crypto.hashPassword(password);

    JsonObject json = new JsonObject();
    json.addProperty("username", username);
    json.addProperty("password", hashedPassword);

    Future<Response<JsonObject>> call =
        Ion.with(context)
            .load(serverUrl(LOGIN_URL))
            .setJsonObjectBody(json)
            .asJsonObject()
            .withResponse();

    BackendResponse<JsonObject, AuthToken> tokenResponse =
        new BackendResponse<>(call, AuthToken.class);

    tokenResponse.onSuccess(new BackendResponse.BackendSuccessListener<AuthToken>() {
      @Override
      public void onSuccess(@NonNull final AuthToken value) {
        saveAuthToken(value.token);
        authUser().onSuccess(new BackendResponse.BackendSuccessListener<User>() {
          @Override
          public void onSuccess(@NonNull final User value) {
            bus.post(value);
          }
        });
      }
    });

    return tokenResponse;
  }

  @NonNull
  public BackendResponse<JsonObject, BackendMessage> logout() {
    BackendResponse<JsonObject, BackendMessage> response =
        new BackendResponse<>(Ion.with(context)
            .load("POST", serverUrl(LOGOUT_URL))
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), BackendMessage.class);

    response.onSuccess(new BackendResponse.BackendSuccessListener<BackendMessage>() {
      @Override
      public void onSuccess(@NonNull final BackendMessage value) {
        clearAuthToken();
        bus.post(User.NO_USER);
      }
    });

    return response;
  }

  @NonNull
  public BackendResponse<JsonObject, User> authUser() {
    BackendResponse<JsonObject, User> response =
        new BackendResponse<>(Ion.with(context)
            .load(serverUrl(AUTH_USER_URL))
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), User.class);

    response.onError(new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(@NonNull final BackendMessage message) {
        if (message.status == HttpURLConnection.HTTP_UNAUTHORIZED) {
          bus.post(User.NO_USER);

          if (!authToken().isEmpty()) {

            Activity currentActivity = DoglegApplication.application().currentActivity();
            if (currentActivity != null) {
              SnackBars.showSimple(currentActivity, "Login expired.");
            }

            clearAuthToken();
          }
        }
      }
    });

    return response;
  }

  @NonNull
  public BackendResponse<JsonObject, User> authAdmin() {
    BackendResponse<JsonObject, User> response =
        new BackendResponse<>(Ion.with(context)
            .load(serverUrl(AUTH_ADMIN_URL))
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), User.class);

    response.onError(new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(@NonNull final BackendMessage message) {
        if (message.status == HttpURLConnection.HTTP_UNAUTHORIZED) {
          bus.post(User.NO_USER);
        }
      }
    });

    return response;
  }
}
