package org.cranst0n.dogleg.android.utils;

import android.support.annotation.NonNull;

public class Objects {

  public static <T> T deepCopy(@NonNull final T object) {
    return (T) Json.pimpedGson().fromJson(Json.pimpedGson().toJson(object), object.getClass());
  }

}
