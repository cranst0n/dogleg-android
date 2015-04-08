package org.cranst0n.dogleg.android.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.cranst0n.dogleg.android.model.Club;
import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class Json {

  private static Gson pimpedGson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
      .registerTypeAdapter(Club.class, new ClubTypeAdapter())
      .create();

  public static Gson pimpedGson() {
    return pimpedGson;
  }

  public static class DateTimeTypeAdapter
      implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    @Override
    public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
      return new JsonPrimitive(src.getMillis());
    }

    @Override
    public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return new DateTime(json.getAsLong());
      } catch (IllegalArgumentException e) {
        return new DateTime(json.getAsString());
      }
    }
  }

  public static class ClubTypeAdapter implements JsonSerializer<Club>, JsonDeserializer<Club> {

    @Override
    public JsonElement serialize(final Club src, final Type typeOfSrc,
                                 final JsonSerializationContext context) {

      JsonObject object = new JsonObject();

      object.addProperty("id", src.id);
      object.addProperty("name", src.name);

      return object;
    }

    @Override
    public Club deserialize(final JsonElement json, final Type typeOfT,
                            final JsonDeserializationContext context) throws JsonParseException {

      JsonObject object = json.getAsJsonObject();

      try {
        return Club.forId(object.get("id").getAsInt());
      } catch (Throwable t0) {
        try {
          return Club.forName(object.get("name").getAsString());
        } catch (Throwable t1) {
          return Club.Unknown;
        }
      }
    }
  }
}
