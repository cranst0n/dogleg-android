package org.cranst0n.dogleg.android.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

public class Colors {

  public static Drawable colorize(final Drawable drawable, final int colorRes, final Context context) {
    drawable.setColorFilter(context.getResources().getColor(colorRes), PorterDuff.Mode.SRC_ATOP);
    return drawable;
  }

  public static int setAlpha(int alpha, int color) {
    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = (color >> 0) & 0xFF;

    return Color.argb(alpha, r, g, b);
  }
}
