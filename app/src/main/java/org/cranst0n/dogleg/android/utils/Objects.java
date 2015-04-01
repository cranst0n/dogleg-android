package org.cranst0n.dogleg.android.utils;

public class Objects {

  public static <T> T deepCopy(final T object) {
    return (T) Json.pimpedGson().fromJson(Json.pimpedGson().toJson(object), object.getClass());
  }

}
