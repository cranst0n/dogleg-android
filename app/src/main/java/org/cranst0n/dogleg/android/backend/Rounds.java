package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.model.Club;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundHandicapResponse;
import org.cranst0n.dogleg.android.utils.Files;
import org.cranst0n.dogleg.android.utils.Json;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Rounds extends BackendComponent {

  private static final String LIST_URL = "/rounds?num=%d&offset=%d";
  private static final String POST_URL = "/rounds";
  private static final String UPDATE_URL = "/rounds";
  private static final String DELETE_URL = "/rounds/%d";

  private static final String HANDICAP_URL = "/handicap?slope=%.2f&numHoles=%d&time=%d";

  public Rounds(@NonNull final Context context) {
    super(context);
  }

  @NonNull
  public BackendResponse<JsonArray, List<Round>> list(final int num, final int offset) {

    String url = String.format(LIST_URL, num, offset);

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url)).setHeader(AuthTokenHeader, authToken())
        .asJsonArray().withResponse(), new TypeToken<List<Round>>() {
    }.getType());
  }

  @NonNull
  public BackendResponse<JsonObject, Round> postRound(@NonNull final Round round) {

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DateTime.class, new Json.DateTimeTypeAdapter())
        .registerTypeAdapter(Club.class, new Json.ClubTypeAdapter())
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

  @NonNull
  public BackendResponse<JsonObject, Round> updateRound(@NonNull final Round round) {

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DateTime.class, new Json.DateTimeTypeAdapter())
        .registerTypeAdapter(Club.class, new Json.ClubTypeAdapter())
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

  @NonNull
  public BackendResponse<JsonObject, BackendMessage> deleteRound(final long roundId) {

    String url = String.format(DELETE_URL, roundId);

    return new BackendResponse<>(Ion.with(context)
        .load("DELETE", serverUrl(url))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), BackendMessage.class);
  }

  @NonNull
  public BackendResponse<JsonObject, RoundHandicapResponse> handicapRound(final double slope,
                                                                          final int numHoles,
                                                                          @NonNull final DateTime time) {

    String url = String.format(HANDICAP_URL, slope, numHoles, time.getMillis());

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(url))
        .setHeader(AuthTokenHeader, authToken())
        .asJsonObject()
        .withResponse(), RoundHandicapResponse.class);
  }

  public boolean backupRoundData(@NonNull final Round round) {

    File backupFile = new File(context.getFilesDir(), String.format("rounds/%d.json", round.id));
    backupFile.getParentFile().mkdirs();
    FileOutputStream outputStream = null;

    try {

      outputStream = new FileOutputStream(backupFile);
      outputStream.write(Json.pimpedGson().toJson(round).getBytes());

      return true;
    } catch (final Exception e) {
      Log.e(Tag, "Failed to write to output stream", e);
      return false;
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (final IOException e) {
          Log.d(Tag, "Failed to close output stream", e);
        }
      }
    }
  }

  public boolean clearBackupRoundData(final long roundId) {
    File backupFile = new File(context.getFilesDir(), String.format("rounds/%d.json", roundId));
    return backupFile.delete();
  }

  @Nullable
  public Round backedUpRound() {

    File backupDir = new File(context.getFilesDir(), "rounds/");
    backupDir.mkdirs();

    try {

      for (File f : backupDir.listFiles()) {
        String jsonString = Files.getStringFromFile(f);
        return Json.pimpedGson().fromJson(jsonString, Round.class);
      }

    } catch (final IOException e) {
      Log.e(Tag, "Failed to parse round file.", e);
    }

    return null;
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
