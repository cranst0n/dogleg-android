package org.cranst0n.dogleg.android.utils;

public class Strings {

  private Strings() {

  }

  public static int levensteinDistance(final String x, final String y) {

    String a = x.toLowerCase();
    String b = y.toLowerCase();

    // i == 0
    int[] costs = new int[b.length() + 1];

    for (int j = 0; j < costs.length; j++) {
      costs[j] = j;
    }

    for (int i = 1; i <= a.length(); i++) {
      // j == 0; nw = lev(i - 1, j)
      costs[0] = i;
      int nw = i - 1;
      for (int j = 1; j <= b.length(); j++) {
        int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
        nw = costs[j];
        costs[j] = cj;
      }
    }
    return costs[b.length()];
  }

  public static String numberSuffix(final int number) {

    if (number >= 11 && number <= 13) {
      return "th";
    }

    switch (number % 10) {
      case 1:
        return "st";
      case 2:
        return "nd";
      case 3:
        return "rd";
      default:
        return "th";
    }
  }
}
