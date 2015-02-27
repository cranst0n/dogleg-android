package org.cranst0n.dogleg.android.backend;

public class BackendMessage {

  public final int status;
  public final String message;
  public final String details;

  public BackendMessage(final int status, final String message, final String details) {
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
