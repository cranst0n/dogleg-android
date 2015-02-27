package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.adapter.DrawerMenuAdapter;
import org.cranst0n.dogleg.android.constants.DrawerMenu;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;

public class NavigationDrawerFragment extends BaseFragment {

  private static final String TAG = NavigationDrawerFragment.class.getSimpleName();

  private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

  private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

  /**
   * A pointer to the current callbacks instance (the Activity).
   */
  private NavigationDrawerCallbacks mCallbacks;

  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerListView;
  private View mFragmentContainerView;
  private int mCurrentSelectedPosition = 0;
  private boolean mFromSavedInstanceState;
  private boolean mUserLearnedDrawer;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Read in the flag indicating whether or not the user has
    // s demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

    if (savedInstanceState != null) {
      mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
      mFromSavedInstanceState = true;
    }
  }


  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {
    mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_drawer_menu, container, false);

    loadListeners();
    loadInfoView();

    return mDrawerListView;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    try {
      mCallbacks = (NavigationDrawerCallbacks) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallbacks = null;
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
  }

  @Override
  public void onConfigurationChanged(final Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Forward the new configuration the drawer toggle component.
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      if (mDrawerLayout != null) {
        if (isDrawerOpen()) {
          mDrawerLayout.closeDrawer(mFragmentContainerView);
        } else {
          mDrawerLayout.openDrawer(mFragmentContainerView);
        }
      } else {
        getActivity().finish();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  public void setDrawerIndicatorEnabled(final boolean enabled) {
    mDrawerToggle.setDrawerIndicatorEnabled(enabled);
  }

  public void openDrawer() {
    if (mDrawerLayout != null && !isDrawerOpen()) {
      mDrawerLayout.openDrawer(mFragmentContainerView);
    }
  }

  public void closeDrawer() {
    if (mDrawerLayout != null && isDrawerOpen()) {
      mDrawerLayout.closeDrawer(mFragmentContainerView);
    }
  }

  private void loadListeners() {
    mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
      }
    });
  }

  private void loadInfoView() {
    DrawerMenuAdapter drawerMenuAdapter = new DrawerMenuAdapter(context, DrawerMenu.MENU_ITEMS);

    mDrawerListView.setAdapter(drawerMenuAdapter);
    mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
  }


  public boolean isDrawerOpen() {
    return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
  }

  public void setUp(final int fragmentId, final DrawerLayout drawerLayout) {

    mFragmentContainerView = getActivity().findViewById(fragmentId);
    mDrawerLayout = drawerLayout;

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.ic_drawer_menu_shadow, GravityCompat.START);
    // set up the drawer's list view with items and click listener


    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    mDrawerToggle = new ActionBarDrawerToggle(
        getActivity(),                    /* host Activity */
        mDrawerLayout,                    /* DrawerLayout object */
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

        if (!mUserLearnedDrawer) {
          // The user manually opened the drawer; store this flag to prevent auto-showing
          // the navigation drawer automatically in the future.
          mUserLearnedDrawer = true;
          SharedPreferences sp = PreferenceManager
              .getDefaultSharedPreferences(getActivity());
          sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
        }

        getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
      }
    };

    // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
    // per the navigation drawer design guidelines.
    if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
      mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    // Defer code dependent on restoration of previous instance state.
    mDrawerLayout.post(new Runnable() {
      @Override
      public void run() {
        mDrawerToggle.syncState();
      }
    });

    mDrawerLayout.setDrawerListener(mDrawerToggle);
  }

  private void selectItem(final int position) {

    mCurrentSelectedPosition = position;

    if (mDrawerListView != null) {
      mDrawerListView.setItemChecked(position, true);
    }
    if (mDrawerLayout != null) {
      mDrawerLayout.closeDrawer(mFragmentContainerView);
    }
    if (mCallbacks != null) {
      mCallbacks.onNavigationDrawerItemSelected(position);
    }
  }

  public static interface NavigationDrawerCallbacks {

    /**
     * Called when an item in the navigation drawer is selected.
     */
    void onNavigationDrawerItemSelected(int position);
  }

}
