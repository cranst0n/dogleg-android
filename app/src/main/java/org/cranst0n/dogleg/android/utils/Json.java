package org.cranst0n.dogleg.android.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class Json {

  private static Gson pimpedGson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

  public static Gson pimpedGson() {
    return pimpedGson;
  }

  public static class DateTimeTypeConverter
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
}
