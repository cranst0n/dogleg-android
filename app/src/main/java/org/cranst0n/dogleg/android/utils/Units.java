package org.cranst0n.dogleg.android.utils;

public class Units {

  private Units() {

  }

  public static double metersToMiles(final double meters) {
    return meters * METERS_2_MILES;
  }

  public static double metersToYards(final double meters) {
    return meters * METERS_2_YARDS;
  }

  private static final double METERS_2_MILES = 0.000621371;
  private static final double METERS_2_YARDS = 1.09361;
}
