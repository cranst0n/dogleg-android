package org.cranst0n.dogleg.android.constants;

import org.cranst0n.dogleg.android.R;

import java.util.ArrayList;
import java.util.Arrays;

public class DrawerMenu {

  public static final int HOME = 0;
  public static final int SETTINGS = 1;

  private static final DrawerMenuItem HOME_ITEM =
      new DrawerMenuItem(R.drawable.ic_action_home, "Home");

  private static final DrawerMenuItem SETTINGS_ITEM =
      new DrawerMenuItem(R.drawable.ic_action_settings, "Settings");

  public static final ArrayList<DrawerMenuItem> MENU_ITEMS =
      new ArrayList<DrawerMenuItem>(Arrays.asList(HOME_ITEM, SETTINGS_ITEM));

  public static class DrawerMenuItem {

    public final int iconRes;
    public final String title;

    public DrawerMenuItem(final int iconRes, final String title) {
      this.iconRes = iconRes;
      this.title = title;
    }

  }
}
