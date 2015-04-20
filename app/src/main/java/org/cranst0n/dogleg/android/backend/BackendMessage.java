package org.cranst0n.dogleg.android.backend;

import android.support.annotation.Nullable;

public class BackendMessage {

  public final int status;
  @Nullable
  public final String message;
  @Nullable
  public final String details;

  public BackendMessage(final int status,
                        @Nullable final String message,
                        @Nullable final String details) {

    this.status = status;
    this.message = message;
    this.details = details;
  }

  public boolean isIncomplete() {
    return status == 0 || message == null || details == null || message.isEmpty();
  }

  @Override
  public String toString() {
    return String.format("[%d] %s: %s", status, message, details);
  }

}
