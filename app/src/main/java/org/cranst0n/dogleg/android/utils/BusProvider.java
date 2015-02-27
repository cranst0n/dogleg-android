package org.cranst0n.dogleg.android.utils;

import com.squareup.otto.Bus;

public class BusProvider {

  private static final String TAG = BusProvider.class.getSimpleName();
  private static Bus mBus;

  public static Bus instance() {
    if (mBus == null) {
      synchronized (BusProvider.class) {
        if (mBus == null) {
          mBus = new Bus();
        }
      }
    }
    return mBus;
  }

}
