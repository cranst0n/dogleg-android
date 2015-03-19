package org.cranst0n.dogleg.android.backend;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;

import java.lang.reflect.Type;

public class Rounds extends BackendComponent {

  private static final String LIST_URL = "/rounds?num=%d&offset=%d";
  private static final String POST_URL = "/rounds";

  private static final String HANDICAP_URL = "/handicap?slope=%.2f&numHoles=%d&time=%d";

  public Rounds(final Context context) {
    super(context);
  }

  public BackendResponse<JsonArray, Round[]> list(final int num, final int offset) {

    String url = String.format(LIST_URL, num, offset);

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url)).setHeader(AuthTokenHeader, authToken())
        .asJsonArray().withResponse(), Round[].class);
  }

  public BackendResponse<JsonObject, Round> postRound(final Round round) {

    Gson gson = new GsonBuilder().registerTypeAdapter(Round.class, new RoundRequestSerializer())
        .create();

    JsonObject jsonObject = gson.toJsonTree(round).getAsJsonObject();

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(POST_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(jsonObject)
        .asJsonObject()
        .withResponse(), Round.class);
  }

  public BackendResponse<JsonObject, RoundHandicapResponse> handicapRound(final double slope, final int numHoles, final long time) {

    String url = String.format(HANDICAP_URL, slope, numHoles, time);

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), RoundHandicapResponse.class);
  }

  private class RoundRequestSerializer implements JsonSerializer<Round> {
    public JsonElement serialize(final Round round, final Type type, final JsonSerializationContext context) {

      JsonObject result = new JsonObject();

      result.addProperty("courseId", round.course.id);
      result.addProperty("ratingId", round.rating.id);
      result.addProperty("time", round.time);
      result.addProperty("official", round.official);

      if (round.isHandicapOverridden) {
        result.addProperty("handicapOverride", round.handicapOverride);
      }

      JsonArray holeScoreJson = new JsonArray();

      for (HoleScore hs : round.holeScores()) {

        JsonObject jo = new JsonObject();

        jo.addProperty("score", hs.score);
        jo.addProperty("netScore", 0);
        jo.addProperty("putts", hs.putts);
        jo.addProperty("penaltyStrokes", hs.penaltyStrokes);
        jo.addProperty("fairwayHit", hs.fairwayHit);
        jo.addProperty("gir", hs.gir);

        JsonObject joHole = new JsonObject();

        joHole.addProperty("id", hs.hole.id);
        joHole.addProperty("number", hs.hole.number);
        joHole.add("features", new JsonArray());

        jo.add("hole", joHole);

        holeScoreJson.add(jo);
      }

      result.add("holeScores", holeScoreJson);

      return result;
    }
  }
}
