package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.AccountActivity;
import org.cranst0n.dogleg.android.activity.AdminActivity;
import org.cranst0n.dogleg.android.activity.HomeActivity;
import org.cranst0n.dogleg.android.activity.ProfileActivity;
import org.cranst0n.dogleg.android.activity.SettingsActivity;
import org.cranst0n.dogleg.android.activity.ShotCaddySetupActivity;
import org.cranst0n.dogleg.android.adapter.DrawerMenuAdapter;
import org.cranst0n.dogleg.android.backend.Authentication;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DrawerFragment extends BaseFragment implements AdapterView.OnItemClickListener {

  private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

  private Bus bus;
  private Users users;
  private User currentUser = User.NO_USER;

  private ActionBarDrawerToggle drawerToggle;
  private DrawerLayout drawerLayout;

  private LinearLayout drawerMainLayout;
  private ListView drawerGeneralListView;
  private ListView drawerUserListView;
  private ListView drawerSettingsListView;

  private CircleImageView userAvatarImage;
  private TextView usernameText;
  private TextView emailText;
  private Button loginButton;
  private Button logoutButton;

  private View fragmentContainerView;
  private int currentSelectedPosition = 0;

  private final DrawerMenuItem homeItem =
      new DrawerMenuItem(R.drawable.ic_action_home, "Home", 0, HomeActivity.class);

  private final DrawerMenuItem profileItem =
      new DrawerMenuItem(R.drawable.ic_profile, "Profile", 450, ProfileActivity.class);

  private final DrawerMenuItem accountItem =
      new DrawerMenuItem(R.drawable.ic_action_account_box, "Account", 500, AccountActivity.class);

  private final DrawerMenuItem adminItem =
      new DrawerMenuItem(R.drawable.ic_action_verified_user, "Admin", 550, AdminActivity.class);

  private final DrawerMenuItem shotCaddyItem =
      new DrawerMenuItem(R.drawable.ic_tee, "Shot Caddy Setup", 950, ShotCaddySetupActivity
          .class);

  private final DrawerMenuItem settingsItem =
      new DrawerMenuItem(R.drawable.ic_action_settings, "Settings", 1000, SettingsActivity.class);

  public final List<DrawerMenuItem> defaultMenuItems =
      new ArrayList<>(Arrays.asList(homeItem));

  public final List<DrawerMenuItem> settingsMenuItems =
      new ArrayList<>(Arrays.asList(shotCaddyItem, settingsItem));

  public final List<DrawerMenuItem> userMenuItems =
      new ArrayList<>(Arrays.asList(profileItem, accountItem));

  public final List<DrawerMenuItem> adminMenuItems =
      new ArrayList<>(Arrays.asList(adminItem));

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
    }

    bus = BusProvider.Instance.bus;
    bus.register(this);

    users = new Users(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
  }

  @Override
  public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                          final long id) {

    currentSelectedPosition = position;

    if (drawerGeneralListView != null) {
      drawerGeneralListView.setItemChecked(position, true);
    }
    if (drawerLayout != null) {
      drawerLayout.closeDrawer(fragmentContainerView);
    }

    if (view.getTag() instanceof DrawerMenuAdapter.ViewHolder) {
      DrawerMenuItem clickedItem = ((DrawerMenuAdapter.ViewHolder) view.getTag()).menuItem;

      if (clickedItem != null) {
        new Handler().post(clickedItem.action);
      }
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    drawerMainLayout =
        (LinearLayout) inflater.inflate(R.layout.fragment_drawer_menu, container, false);

    drawerGeneralListView = (ListView) drawerMainLayout.findViewById(R.id.drawer_general_items_list);
    drawerUserListView = (ListView) drawerMainLayout.findViewById(R.id.drawer_user_items_list);
    drawerSettingsListView = (ListView) drawerMainLayout.findViewById(R.id
        .drawer_settings_items_list);

    drawerGeneralListView.setOnItemClickListener(this);
    drawerUserListView.setOnItemClickListener(this);
    drawerSettingsListView.setOnItemClickListener(this);

    drawerGeneralListView.setAdapter(new DrawerMenuAdapter(context, defaultMenuItems));
    drawerUserListView.setAdapter(new DrawerMenuAdapter(context, userMenuItems));
    drawerSettingsListView.setAdapter(new DrawerMenuAdapter(context, settingsMenuItems));

    userAvatarImage = (CircleImageView) drawerMainLayout.findViewById(R.id.user_avatar);
    usernameText = (TextView) drawerMainLayout.findViewById(R.id.username_text);
    emailText = (TextView) drawerMainLayout.findViewById(R.id.email_text);
    loginButton = (Button) drawerMainLayout.findViewById(R.id.login_button);
    logoutButton = (Button) drawerMainLayout.findViewById(R.id.logout_button);

    userAvatarImage.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
    usernameText.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
    emailText.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
    logoutButton.setVisibility(currentUser.isValid() ? View.VISIBLE : View.GONE);
    loginButton.setVisibility(currentUser.isValid() ? View.GONE : View.VISIBLE);

    usernameText.setText(currentUser.name);
    emailText.setText(currentUser.email);

    logoutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        drawerLayout.closeDrawer(fragmentContainerView);
        new Authentication(context).logout();
      }
    });

    loginButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        drawerLayout.closeDrawer(fragmentContainerView);
        Dialogs.showLoginDialog(activity);
      }
    });

    loadUserAvatar();

    return drawerMainLayout;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    drawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      if (drawerLayout != null) {
        if (isDrawerOpen()) {
          drawerLayout.closeDrawer(fragmentContainerView);
          return true;
        } else if (drawerToggle.isDrawerIndicatorEnabled()) {
          drawerLayout.openDrawer(fragmentContainerView);
          return true;
        }
      } else {
        getActivity().finish();
      }
    }

    return super.onOptionsItemSelected(item);
  }

  @Subscribe
  public void newUser(final User user) {

    this.currentUser = user;

    userMenuItems.removeAll(adminMenuItems);

    if (user.isValid()) {

      loadUserAvatar();

      if (user.admin) {
        userMenuItems.addAll(adminMenuItems);
      }
    }

    if (usernameText != null) {

      userAvatarImage.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
      usernameText.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
      emailText.setVisibility(currentUser.isValid() ? View.VISIBLE : View.INVISIBLE);
      logoutButton.setVisibility(currentUser.isValid() ? View.VISIBLE : View.GONE);
      loginButton.setVisibility(currentUser.isValid() ? View.GONE : View.VISIBLE);

      usernameText.setText(user.name);
      emailText.setText(user.email);
    }

    if (drawerGeneralListView != null) {

      drawerUserListView.setVisibility(user.isValid() ? View.VISIBLE : View.GONE);

      ((DrawerMenuAdapter) drawerGeneralListView.getAdapter()).notifyDataSetChanged();
      ((DrawerMenuAdapter) drawerUserListView.getAdapter()).notifyDataSetChanged();
      ((DrawerMenuAdapter) drawerSettingsListView.getAdapter()).notifyDataSetChanged();
    }
  }

  private void loadUserAvatar() {
    if (userAvatarImage != null && currentUser.isValid()) {
      Ion.with(this)
          .load(users.avatarUrl(currentUser))
          .noCache()
          .asBitmap()
          .setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(final Exception e, final Bitmap result) {
              if (e == null) {
                userAvatarImage.setImageBitmap(result);
              }
            }
          });
    }
  }

  public void setDrawerIndicatorEnabled(final boolean enabled) {
    drawerToggle.setDrawerIndicatorEnabled(enabled);
  }

  public boolean isDrawerOpen() {
    return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
  }

  public void setUp(final int fragmentId, final DrawerLayout drawerLayout) {

    fragmentContainerView = getActivity().findViewById(fragmentId);
    this.drawerLayout = drawerLayout;

    // set a custom shadow that overlays the main content when the drawer opens
    this.drawerLayout.setDrawerShadow(R.drawable.ic_drawer_menu_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    drawerToggle = new ActionBarDrawerToggle(
        getActivity(),                    /* host Activity */
        DrawerFragment.this.drawerLayout,                    /* DrawerLayout object */
        R.string.app_name,  /* "open drawer" description for accessibility */
        R.string.app_name  /* "close drawer" description for accessibility */
    ) {
      @Override
      public void onDrawerClosed(final View drawerView) {
        super.onDrawerClosed(drawerView);
        if (!isAdded()) {
          return;
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }

      @Override
      public void onDrawerOpened(final View drawerView) {
        super.onDrawerOpened(drawerView);
        if (!isAdded()) {
          return;
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
    drawerToggle.setHomeAsUpIndicator(upArrow);

    // Defer code dependent on restoration of previous instance state.
    this.drawerLayout.post(new Runnable() {
      @Override
      public void run() {
        drawerToggle.syncState();
      }
    });

    this.drawerLayout.setDrawerListener(drawerToggle);
  }

  public class DrawerMenuItem implements Comparable<DrawerMenuItem> {

    public final int iconRes;
    public final String title;
    public final Runnable action;
    public final int priority;

    public final boolean experimental;

    public DrawerMenuItem(@DrawableRes final int iconRes, @NonNull final String title,
                          final int priority, @NonNull final Class<?> activityToStart) {
      this(iconRes, title, priority, activityToStart, false);
    }

    public DrawerMenuItem(@DrawableRes final int iconRes, @NonNull final String title,
                          final int priority, @NonNull final Class<?> activityToStart,
                          final boolean experimental) {

      this(iconRes, title, priority, new Runnable() {
        @Override
        public void run() {
          if (activity.getClass() != activityToStart) {
            Intent intent = new Intent(context, activityToStart);
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
          } else {
            drawerLayout.closeDrawers();
          }
        }
      }, experimental);
    }

    public DrawerMenuItem(@DrawableRes final int iconRes, @NonNull final String title,
                          final int priority, @NonNull final Runnable action,
                          final boolean experimental) {

      this.iconRes = iconRes;
      this.title = title;
      this.priority = priority;
      this.action = action;
      this.experimental = experimental;
    }

    @Override
    public int compareTo(@NonNull final DrawerMenuItem drawerMenuItem) {
      return this.priority - drawerMenuItem.priority;
    }
  }

}
