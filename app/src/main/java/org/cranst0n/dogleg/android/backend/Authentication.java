package org.cranst0n.dogleg.android.backend;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.apache.http.HttpStatus;
import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.model.AuthToken;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Crypto;
import org.cranst0n.dogleg.android.utils.SnackBarUtils;

public class Authentication extends BackendComponent {

  private static final String LOGIN_URL = "/login";
  private static final String LOGOUT_URL = "/logout";
  private static final String AUTH_USER_URL = "/authuser";
  private static final String AUTH_ADMIN_URL = "/authadmin";

  public Authentication(final Context context) {
    super(context);
  }

  public BackendResponse<JsonObject, AuthToken> login(final String username, final String password) {

    String hashedPassword = Crypto.hashPassword(password);

    JsonObject json = new JsonObject();
    json.addProperty("username", username);
    json.addProperty("password", hashedPassword);

    Future<Response<JsonObject>> call =
        Ion.with(context)
            .load(serverUrl() + LOGIN_URL)
            .setJsonObjectBody(json)
            .asJsonObject()
            .withResponse();

    BackendResponse<JsonObject, AuthToken> tokenResponse =
        new BackendResponse<JsonObject, AuthToken>(call, AuthToken.class);

    tokenResponse.onSuccess(new BackendResponse.BackendSuccessListener<AuthToken>() {
      @Override
      public void onSuccess(final AuthToken value) {
        saveAuthToken(value.token);
        authUser().onSuccess(new BackendResponse.BackendSuccessListener<User>() {
          @Override
          public void onSuccess(final User value) {
            bus.post(value);
          }
        });
      }
    });

    return tokenResponse;
  }

  public BackendResponse<JsonObject, BackendMessage> logout() {
    BackendResponse<JsonObject, BackendMessage> response =
        new BackendResponse<JsonObject, BackendMessage>(Ion.with(context)
            .load("POST", url(LOGOUT_URL))
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), BackendMessage.class);

    response.onSuccess(new BackendResponse.BackendSuccessListener<BackendMessage>() {
      @Override
      public void onSuccess(final BackendMessage value) {
        clearAuthToken();
        bus.post(User.NO_USER);
      }
    });

    return response;
  }

  public BackendResponse<JsonObject, User> authUser() {
    BackendResponse<JsonObject, User> response =
        new BackendResponse<JsonObject, User>(Ion.with(context)
            .load(serverUrl() + AUTH_USER_URL)
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), User.class);

    response.onError(new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(final BackendMessage message) {
        if (message.status == HttpStatus.SC_UNAUTHORIZED) {
          bus.post(User.NO_USER);

          if (!authToken().isEmpty()) {
            SnackBarUtils.showSimple(DoglegApplication.application().currentActivity(), "Login expired.");
            clearAuthToken();
          }
        }
      }
    });

    return response;
  }

  public BackendResponse<JsonObject, User> authAdmin() {
    BackendResponse<JsonObject, User> response =
        new BackendResponse<JsonObject, User>(Ion.with(context)
            .load(serverUrl() + AUTH_ADMIN_URL)
            .setHeader(AuthTokenHeader, authToken())
            .asJsonObject()
            .withResponse(), User.class);

    response.onError(new BackendResponse.BackendErrorListener() {
      @Override
      public void onError(final BackendMessage message) {
        if (message.status == HttpStatus.SC_UNAUTHORIZED) {
          bus.post(User.NO_USER);
        }
      }
    });

    return response;
  }
}
