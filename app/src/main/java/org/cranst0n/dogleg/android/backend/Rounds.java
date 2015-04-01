package org.cranst0n.dogleg.android.backend;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.utils.Json;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.List;

public class Rounds extends BackendComponent {

  private static final String LIST_URL = "/rounds?num=%d&offset=%d";
  private static final String POST_URL = "/rounds";
  private static final String UPDATE_URL = "/rounds";
  private static final String DELETE_URL = "/rounds/%d";

  private static final String HANDICAP_URL = "/handicap?slope=%.2f&numHoles=%d&time=%d";

  public Rounds(final Context context) {
    super(context);
  }

  public BackendResponse<JsonArray, List<Round>> list(final int num, final int offset) {

    String url = String.format(LIST_URL, num, offset);

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url)).setHeader(AuthTokenHeader, authToken())
        .asJsonArray().withResponse(), new TypeToken<List<Round>>() {
    }.getType());
  }

  public BackendResponse<JsonObject, Round> postRound(final Round round) {

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DateTime.class, new Json.DateTimeTypeConverter())
        .registerTypeAdapter(Round.class, new RoundRequestSerializer())
        .create();

    JsonObject jsonObject = gson.toJsonTree(round).getAsJsonObject();

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(POST_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(jsonObject)
        .asJsonObject()
        .withResponse(), Round.class);
  }

  public BackendResponse<JsonObject, Round> updateRound(final Round round) {

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DateTime.class, new Json.DateTimeTypeConverter())
        .registerTypeAdapter(Round.class, new RoundSerializer())
        .create();

    JsonObject roundObject = gson.toJsonTree(round).getAsJsonObject();

    return new BackendResponse<>(Ion.with(context)
        .load("PUT", serverUrl(UPDATE_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(roundObject)
        .asJsonObject()
        .withResponse(), Round.class);
  }

  public BackendResponse<JsonObject, BackendMessage> deleteRound(final long roundId) {

    String url = String.format(DELETE_URL, roundId);

    return new BackendResponse<>(Ion.with(context)
        .load("DELETE", serverUrl(url))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), BackendMessage.class);
  }

  public BackendResponse<JsonObject, RoundHandicapResponse> handicapRound(final double slope,
                                                                          final int numHoles,
                                                                          final DateTime time) {

    String url = String.format(HANDICAP_URL, slope, numHoles, time.getMillis());

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), RoundHandicapResponse.class);
  }

  private class RoundRequestSerializer implements JsonSerializer<Round> {
    public JsonElement serialize(final Round round, final Type type,
                                 final JsonSerializationContext context) {

      JsonObject result = new JsonObject();

      result.addProperty("courseId", round.course.id);
      result.addProperty("ratingId", round.rating.id);
      result.addProperty("time", round.time.getMillis());
      result.addProperty("official", round.official);

      if (round.isHandicapOverridden) {
        result.addProperty("handicapOverride", round.handicapOverride);
      }

      result.add("holeScores", context.serialize(round.holeScores()));

      return result;
    }
  }

  private class RoundSerializer implements JsonSerializer<Round> {
    public JsonElement serialize(final Round round, final Type type,
                                 final JsonSerializationContext context) {

      JsonObject result = new JsonObject();

      result.addProperty("id", round.id);
      result.add("user", context.serialize(round.user));
      result.add("course", context.serialize(round.course));
      result.add("rating", context.serialize(round.rating));
      result.addProperty("time", round.time.getMillis());
      result.addProperty("official", round.official);
      result.addProperty("handicap", round.handicap);

      if (round.isHandicapOverridden) {
        result.addProperty("handicapOverride", round.handicapOverride);
      }

      result.add("holeScores", context.serialize(round.holeScores()));

      return result;
    }
  }
}
