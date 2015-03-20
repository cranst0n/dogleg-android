package org.cranst0n.dogleg.android.utils;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Time {

  private static final List<String> timesString =
      Arrays.asList("year", "month", "day", "hour", "minute", "second");

  private static final List<Long> times = Arrays.asList(
      TimeUnit.DAYS.toMillis(365),
      TimeUnit.DAYS.toMillis(30),
      TimeUnit.DAYS.toMillis(1),
      TimeUnit.HOURS.toMillis(1),
      TimeUnit.MINUTES.toMillis(1),
      TimeUnit.SECONDS.toMillis(1));

  private Time() {

  }

  public static String ago(final long time) {
    return ago(new DateTime(time));
  }

  public static String ago(final DateTime dateTime) {

    StringBuffer res = new StringBuffer();

    for (int i = 0; i < times.size(); i++) {

      Long current = times.get(i);
      long temp = (System.currentTimeMillis() - dateTime.getMillis()) / current;

      if (temp > 0) {
        res.append(temp).append(" ").
            append(timesString.get(i)).
            append(temp > 1 ? "s" : "").
            append(" ago");

        break;
      }
    }

    if ("".equals(res.toString()))
      return "Just now";
    else
      return res.toString();
  }
}
