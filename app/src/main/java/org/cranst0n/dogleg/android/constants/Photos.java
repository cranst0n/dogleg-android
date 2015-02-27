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
      R.drawable.stock_photo_15
  };

  public static final int random() {
    return stock[new Random().nextInt(stock.length)];
  }

  public static final int photoFor(final int value) {
    return stock[value % stock.length];
  }
}
