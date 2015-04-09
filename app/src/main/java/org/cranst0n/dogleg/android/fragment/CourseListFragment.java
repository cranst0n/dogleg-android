package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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

import com.google.gson.JsonArray;
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

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class CourseListFragment extends BaseFragment implements SearchView.OnQueryTextListener {

  private Bus bus;
  private User currentUser = User.NO_USER;

  private Location lastLocation;

  private View courseListView;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView recyclerView;
  private FloatingActionButton fab;
  private SmoothProgressBar appendInProgressBar;
  private TextView noCoursesIndicator;

  private BackendResponse<JsonArray, List<CourseSummary>> queryCall;

  private boolean pinMode = false;
  private MenuItem pinnedMenuItem;
  private PinSwitch pinnedCoursesSwitch;
  private MenuItem courseSearchMenuItem;
  private SearchView courseSearchView;
  private boolean refreshOnSearchClose = false;

  private Courses courses;

  private final List<CourseSummary> displayedCourseList = new ArrayList<>();

  private int previousTotal = 0;
  private boolean loading = true;
  private boolean endOfListReached = false;
  private final int visibleThreshold = 5;
  private int firstVisibleItem, visibleItemCount, totalItemCount;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    setHasOptionsMenu(true);

    lastLocation = DoglegApplication.application().lastKnownLocation();
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
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    courses = new Courses(context);
    courseListView = inflater.inflate(R.layout.fragment_course_list, container, false);

    swipeRefreshLayout = (SwipeRefreshLayout) courseListView.findViewById(R.id.swipe_refresh_container);
    recyclerView = (RecyclerView) courseListView.findViewById(R.id.course_list_recycler);
    fab = (FloatingActionButton) courseListView.findViewById(R.id.course_list_fab);
    appendInProgressBar = (SmoothProgressBar) courseListView.findViewById(R.id.append_in_progress_bar);
    noCoursesIndicator = (TextView) courseListView.findViewById(R.id.course_list_none_indicator);

    swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);

    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        addToCourseList(false);
      }
    });

    initRecyclerView();

    fab.attachToRecyclerView(recyclerView);
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

    addToCourseList(false);
    setHasOptionsMenu(true);

    return courseListView;
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.course_list_fragment_menu, menu);

    courseSearchMenuItem = menu.findItem(R.id.action_course_search);
    courseSearchView = (SearchView) MenuItemCompat.getActionView(courseSearchMenuItem);

    Views.colorizeSearchView(courseSearchView, android.R.color.white, context);

    pinnedMenuItem = menu.findItem(R.id.pinned_switch);
    pinnedCoursesSwitch = (PinSwitch) MenuItemCompat.getActionView(pinnedMenuItem);
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
  public String getTitle() {
    return "Courses";
  }

  private void initRecyclerView() {

    final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(new CourseListRecyclerAdapter(activity, displayedCourseList));

    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override
      public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = CourseListFragment.this.recyclerView.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

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
      queryCall =
          courses.searchPinned(courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else if (pinMode) {
      queryCall =
          courses.listPinned(LatLon.fromLocation(lastLocation), 20, displayedCourseList.size());
    } else if (searchMode()) {
      queryCall =
          courses.search(courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else {
      queryCall = courses.list(LatLon.fromLocation(lastLocation), 20, displayedCourseList.size());
    }

    queryCall.
        onSuccess(new BackendResponse.BackendSuccessListener<List<CourseSummary>>() {
          @Override
          public void onSuccess(final List<CourseSummary> value) {
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

}
