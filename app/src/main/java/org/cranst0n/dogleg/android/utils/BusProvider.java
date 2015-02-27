package org.cranst0n.dogleg.android.utils;

import com.squareup.otto.Bus;

public enum BusProvider {

  Instance(new Bus());

  private BusProvider(final Bus bus) {
    this.bus = bus;
  }

  public final Bus bus;

}
