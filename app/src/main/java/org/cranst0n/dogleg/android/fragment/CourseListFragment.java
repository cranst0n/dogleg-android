package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.JsonElement;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.CourseRequestActivity;
import org.cranst0n.dogleg.android.adapter.CourseListRecyclerAdapter;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.model.CourseSummary;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.views.PinSwitch;
import org.cranst0n.dogleg.android.views.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class CourseListFragment extends BaseFragment implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    SearchView.OnQueryTextListener {

  private Bus bus;
  private User currentUser = User.NO_USER;

  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView recyclerView;
  private FloatingActionButton fab;
  private SmoothProgressBar appendInProgressBar;
  private TextView noCoursesIndicator;

  private BackendResponse<? extends JsonElement, List<CourseSummary>> queryCall;

  private boolean pinMode = false;
  private MenuItem courseSearchMenuItem;
  private SearchView courseSearchView;
  private boolean refreshOnSearchClose = false;

  private Courses courses;

  private final List<CourseSummary> summaryListCache = new ArrayList<>();
  private final List<CourseSummary> displayedCourseList = new ArrayList<>();

  private int previousTotal = 0;
  private int lastKnownFirstPosition;
  private boolean loading = true;
  private boolean endOfListReached = false;
  private final int visibleThreshold = 8;
  private int firstVisibleItem, visibleItemCount, totalItemCount;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    setHasOptionsMenu(true);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);

    if (queryCall != null) {
      queryCall.cancel();
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    courses = new Courses(context);
    View courseListView = inflater.inflate(R.layout.fragment_course_list, container, false);

    swipeRefreshLayout = (SwipeRefreshLayout) courseListView.findViewById(R.id.swipe_refresh_container);
    recyclerView = (RecyclerView) courseListView.findViewById(R.id.course_list_recycler);
    fab = (FloatingActionButton) courseListView.findViewById(R.id.course_list_fab);
    appendInProgressBar = (SmoothProgressBar) courseListView.findViewById(R.id.append_in_progress_bar);
    noCoursesIndicator = (TextView) courseListView.findViewById(R.id.course_list_none_indicator);

    swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);

    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        addToCourseList(false, true);  // Always fetch from backend (ignore cache)
      }
    });

    initRecyclerView();

    fab.setVisibility(currentUser.isValid() ? View.VISIBLE : View.GONE);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        if (currentUser.isValid()) {
          startActivity(new Intent(activity, CourseRequestActivity.class));
        } else {
          SnackBars.showSimple(activity, "Must be logged in to request course.");
        }
      }
    });

    setHasOptionsMenu(true);

    // Wait for the client to connect or fail so we can send our location (if possible) to get
    // courses closest to current location
    app.googleApiClient().registerConnectionCallbacks(this);
    app.googleApiClient().registerConnectionFailedListener(this);

    return courseListView;
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.course_list_fragment_menu, menu);

    courseSearchMenuItem = menu.findItem(R.id.action_course_search);
    courseSearchView = (SearchView) MenuItemCompat.getActionView(courseSearchMenuItem);

    Views.colorizeSearchView(courseSearchView, android.R.color.white, context);

    MenuItem pinnedMenuItem = menu.findItem(R.id.pinned_switch);
    PinSwitch pinnedCoursesSwitch = (PinSwitch) MenuItemCompat.getActionView(pinnedMenuItem);
    pinnedCoursesSwitch.setChecked(pinMode);
    pinnedCoursesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        pinMode = isChecked;
        addToCourseList(false);
      }
    });

    courseSearchView.setOnSearchClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        for (int ix = 0; ix < menu.size(); ix++) {
          if (menu.getItem(ix) != courseSearchMenuItem) {
            menu.getItem(ix).setVisible(false);
          }
        }
        courseSearchView.requestFocus();
      }
    });
    courseSearchView.setOnQueryTextListener(this);
    courseSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
      @Override
      public boolean onClose() {

        for (int ix = 0; ix < menu.size(); ix++) {
          menu.getItem(ix).setVisible(true);
        }

        activity.invalidateOptionsMenu();

        if (refreshOnSearchClose) {
          addToCourseList(false);
        }

        refreshOnSearchClose = false;

        return false;
      }
    });
  }

  @Override
  public boolean onQueryTextSubmit(final String s) {
    refreshOnSearchClose = true;
    addToCourseList(false);
    return true;
  }

  @Override
  public boolean onQueryTextChange(final String s) {
    if (s.length() > 1) {
      refreshOnSearchClose = true;
      addToCourseList(false);
    }
    return true;
  }

  @Override
  @NonNull
  public String getTitle() {
    return "Courses";
  }

  private void initRecyclerView() {

    final LinearLayoutManager layoutManager = new LinearLayoutManager(context,
        LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(new CourseListRecyclerAdapter(activity, displayedCourseList));

    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override
      public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {

        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = CourseListFragment.this.recyclerView.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (layoutManager.findFirstVisibleItemPosition() > lastKnownFirstPosition) {
          fab.hide();
        } else if (layoutManager.findFirstVisibleItemPosition() < lastKnownFirstPosition) {
          fab.show();
        }

        lastKnownFirstPosition = layoutManager.findFirstVisibleItemPosition();

        if (loading) {
          if (totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
          }
        }

        if (!loading && !endOfListReached && dy > 0 && (totalItemCount - visibleItemCount)
            <= (firstVisibleItem + visibleThreshold)) {

          addToCourseList(true);
        }
      }
    });
  }

  @Subscribe
  public void newUser(final User user) {

    currentUser = user;

    if (fab != null) {
      fab.setVisibility(currentUser.isValid() ? View.VISIBLE : View.GONE);
    }
  }

  private boolean searchMode() {
    return courseSearchView != null && courseSearchView.getQuery().length() > 0;
  }

  private void addToCourseList(final boolean append) {
    addToCourseList(append, false);
  }

  private void addToCourseList(final boolean append, final boolean ignoreCache) {

    loading = true;
    noCoursesIndicator.setVisibility(View.GONE);

    if (append) {
      appendInProgressBar.setVisibility(View.VISIBLE);
    } else {
      recyclerView.setVisibility(View.INVISIBLE);
      displayedCourseList.clear();
      endOfListReached = false;

      // Workaround to address bug: https://code.google.com/p/android/issues/detail?id=77712
      swipeRefreshLayout.post(new Runnable() {
        @Override
        public void run() {
          swipeRefreshLayout.setRefreshing(true);
        }
      });
    }

    if (queryCall != null) {
      queryCall.cancel();
    }

    if (pinMode && searchMode()) {
      queryCall = courses.searchPinned(
          courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else if (pinMode) {
      queryCall =
          courses.listPinned(
              LatLon.fromLocation(DoglegApplication.application().lastKnownLocation()), 20,
              displayedCourseList.size());
    } else if (searchMode()) {
      queryCall =
          courses.search(courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else {

      if (append || summaryListCache.isEmpty() || ignoreCache) {
        queryCall = courses.list(
            LatLon.fromLocation(DoglegApplication.application().lastKnownLocation()), 20,
            displayedCourseList.size()).onSuccess(new BackendResponse.BackendSuccessListener<List<CourseSummary>>() {
          @Override
          public void onSuccess(@NonNull List<CourseSummary> value) {
            summaryListCache.addAll(value);
          }
        });

      } else {
        queryCall = BackendResponse.fromCallable(new Callable<List<CourseSummary>>() {
          @Override
          public List<CourseSummary> call() throws Exception {
            return summaryListCache;
          }
        });
      }
    }

    queryCall.
        onSuccess(new BackendResponse.BackendSuccessListener<List<CourseSummary>>() {
          @Override
          public void onSuccess(@NonNull final List<CourseSummary> value) {
            endOfListReached = value.isEmpty();
            displayedCourseList.addAll(value);
          }
        }).
        onFinally(new BackendResponse.BackendFinallyListener() {
          @Override
          public void onFinally() {
            loading = false;
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            appendInProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (displayedCourseList.isEmpty()) {
              noCoursesIndicator.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  // These methods are only used for the initial list population since we want to wait for the
  // GoogleApiClient to connect/fail before making our list request

  @Override
  public void onConnected(final Bundle bundle) {
    addToCourseList(false);
    app.googleApiClient().unregisterConnectionCallbacks(this);
    app.googleApiClient().unregisterConnectionFailedListener(this);
  }

  @Override
  public void onConnectionSuspended(final int i) {
    app.googleApiClient().unregisterConnectionCallbacks(this);
    app.googleApiClient().unregisterConnectionFailedListener(this);
  }

  @Override
  public void onConnectionFailed(final ConnectionResult connectionResult) {
    addToCourseList(false);
    app.googleApiClient().unregisterConnectionCallbacks(this);
    app.googleApiClient().unregisterConnectionFailedListener(this);
  }

}
