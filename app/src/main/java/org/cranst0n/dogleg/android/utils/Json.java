package org.cranst0n.dogleg.android.utils;

import android.util.Base64;

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

  private static final Gson pimpedGson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
      .registerTypeAdapter(byte[].class, new ByteArrayToBase64TypeAdapter())
      .registerTypeAdapter(Club.class, new ClubTypeAdapter())
      .create();

  public static Gson pimpedGson() {
    return pimpedGson;
  }

  public static class DateTimeTypeAdapter
      implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {
    @Override
    public JsonElement serialize(final DateTime src, final Type srcType,
                                 final JsonSerializationContext context) {
      return new JsonPrimitive(src.getMillis());
    }

    @Override
    public DateTime deserialize(final JsonElement json, final Type type,
                                final JsonDeserializationContext
                                    context)
        throws JsonParseException {
      try {
        return new DateTime(json.getAsLong());
      } catch (IllegalArgumentException e) {
        return new DateTime(json.getAsString());
      }
    }
  }

  public static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>,
      JsonDeserializer<byte[]> {
    public byte[] deserialize(final JsonElement json, final Type typeOfT,
                              final JsonDeserializationContext context) throws JsonParseException {
      return Base64.decode(json.getAsString(), Base64.NO_WRAP);
    }

    public JsonElement serialize(final byte[] src, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
      return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
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
