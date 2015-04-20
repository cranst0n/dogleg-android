package org.cranst0n.dogleg.android.utils;

import android.support.annotation.NonNull;

import com.squareup.otto.Bus;

public enum BusProvider {

  Instance(new Bus());

  BusProvider(@NonNull final Bus bus) {
    this.bus = bus;
  }

  @NonNull
  public final Bus bus;

}
