package org.cranst0n.dogleg.android.model;

import android.support.annotation.NonNull;

import org.cranst0n.dogleg.android.utils.Strings;

public enum Club {

  Driver(0, "Driver"),
  // Woods
  Wood2(102, "2 Wood"),
  Wood3(103, "3 Wood"),
  Wood4(104, "4 Wood"),
  Wood5(105, "5 Wood"),
  Wood7(107, "7 Wood"),
  Wood9(109, "9 Wood"),
  Wood11(111, "11 Wood"),
  // Hybrids
  Hybrid1(201, "1 Hybrid"),
  Hybrid2(202, "2 Hybrid"),
  Hybrid3(203, "3 Hybrid"),
  Hybrid4(204, "4 Hybrid"),
  Hybrid5(205, "5 Hybrid"),
  Hybrid6(206, "6 Hybrid"),
  Hybrid7(207, "7 Hybrid"),
  Hybrid8(208, "8 Hybrid"),
  Hybrid9(209, "9 Hybrid"),
  // Irons
  Iron1(301, "1 Iron"),
  Iron2(302, "2 Iron"),
  Iron3(303, "3 Iron"),
  Iron4(304, "4 Iron"),
  Iron5(305, "5 Iron"),
  Iron6(306, "6 Iron"),
  Iron7(307, "7 Iron"),
  Iron8(308, "8 Iron"),
  Iron9(309, "9 Iron"),
  // Wedges
  WedgeP(445, "Pitching Wedge"),
  Wedge50(450, String.format("50%s Wedge", Strings.DEGREE)),
  Wedge52(452, String.format("52%s Wedge", Strings.DEGREE)),
  Wedge54(454, String.format("54%s Wedge", Strings.DEGREE)),
  Wedge56(456, String.format("56%s Wedge", Strings.DEGREE)),
  Wedge58(458, String.format("58%s Wedge", Strings.DEGREE)),
  Wedge60(460, String.format("60%s Wedge", Strings.DEGREE)),
  Wedge62(462, String.format("62%s Wedge", Strings.DEGREE)),
  Wedge64(464, String.format("64%s Wedge", Strings.DEGREE)),
  Putter(500, "Putter"),
  Unknown(1000, "Unknown"),
  // Other Actions
  FinishHole(2000, "Finish Hole");

  Club(final int id, @NonNull final String name) {
    this.id = id;
    this.name = name;
  }

  public final int id;
  @NonNull
  public final String name;

  @NonNull
  public static Club forId(final int id) {
    for (Club club : values()) {
      if (club.id == id) {
        return club;
      }
    }

    return Club.Unknown;
  }

  @NonNull
  public static Club forName(@NonNull final String name) {
    for (Club club : values()) {
      if (club.name.equalsIgnoreCase(name)) {
        return club;
      }
    }

    return Club.Unknown;
  }
}
