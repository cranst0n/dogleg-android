package org.cranst0n.dogleg.android.constants;

import org.cranst0n.dogleg.android.R;

import java.util.Random;

public class Photos {

  private static final int[] stock = {
      R.drawable.stock_photo_0,
      R.drawable.stock_photo_1,
      R.drawable.stock_photo_2,
      R.drawable.stock_photo_3,
      R.drawable.stock_photo_4,
      R.drawable.stock_photo_5,
      R.drawable.stock_photo_6,
      R.drawable.stock_photo_7,
      R.drawable.stock_photo_8,
      R.drawable.stock_photo_9,
      R.drawable.stock_photo_10,
      R.drawable.stock_photo_11,
      R.drawable.stock_photo_12,
      R.drawable.stock_photo_13,
      R.drawable.stock_photo_14,
      R.drawable.stock_photo_15,
      R.drawable.stock_photo_16,
      R.drawable.stock_photo_17,
      R.drawable.stock_photo_18,
      R.drawable.stock_photo_19,
      R.drawable.stock_photo_20,
      R.drawable.stock_photo_21,
      R.drawable.stock_photo_22,
      R.drawable.stock_photo_23,
      R.drawable.stock_photo_24,
      R.drawable.stock_photo_25,
      R.drawable.stock_photo_26,
      R.drawable.stock_photo_27,
      R.drawable.stock_photo_28,
      R.drawable.stock_photo_29
  };

  public static final int random() {
    return stock[new Random().nextInt(stock.length)];
  }

  public static final int photoFor(final long value) {
    return stock[(int) value % stock.length];
  }
}
