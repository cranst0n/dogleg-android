package org.cranst0n.dogleg.android.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.adapter.CourseListRecyclerAdapter;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseSummary;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.views.PinSwitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseListFragment extends BaseFragment implements SearchView.OnQueryTextListener,
    SearchView.OnCloseListener {

  private Location lastLocation;

  private View mViewFragmentCourseList;
  private RecyclerView mRecyclerView;

  private BackendResponse<JsonArray, CourseSummary[]> queryCall;

  private boolean pinMode = false;
  private PinSwitch pinnedCoursesSwitch;
  private SearchView courseSearchView;
  private ProgressBar searchingProgressView;

  private Courses courses;

  private List<CourseSummary> displayedCourseList = new ArrayList<CourseSummary>();

  private int previousTotal = 0;
  private boolean loading = true;
  private int visibleThreshold = 5;
  private int firstVisibleItem, visibleItemCount, totalItemCount;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if(savedInstanceState != null) {
      boolean pm = savedInstanceState.getBoolean("pinMod");
      Log.d(getClass().getSimpleName(), ">>>>> pm: " + pm);
    }

    lastLocation = DoglegApplication.lastKnownLocation();
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    courses = new Courses(context);
    mViewFragmentCourseList = inflater.inflate(R.layout.fragment_course_list, container, false);

    mRecyclerView = (RecyclerView) mViewFragmentCourseList.findViewById(R.id.fragment_course_list_content_main);
    searchingProgressView = (ProgressBar) mViewFragmentCourseList.findViewById(R.id.search_in_progress_bar);

    initRecyclerView();
    addToCourseList(false);

    setHasOptionsMenu(true);

    return mViewFragmentCourseList;
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.course_list_fragment_menu, menu);

    courseSearchView =
        (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_course_search));
    pinnedCoursesSwitch = (PinSwitch) MenuItemCompat.getActionView(menu.findItem(R.id.pinned_switch));
    pinnedCoursesSwitch.setChecked(pinMode);
    pinnedCoursesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        pinMode = isChecked;
        addToCourseList(false);
      }
    });

    courseSearchView.setOnQueryTextListener(this);
    courseSearchView.setOnCloseListener(this);
  }

  @Override
  public boolean onQueryTextSubmit(final String s) {
    addToCourseList(false);
    return true;
  }

  @Override
  public boolean onQueryTextChange(final String s) {
    if(s.length() > 1) {
      addToCourseList(false);
    }
    return true;
  }

  @Override
  public boolean onClose() {
    addToCourseList(false);
    return false;
  }

  private void initRecyclerView() {

    final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(new CourseListRecyclerAdapter(activity, displayedCourseList));

    mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override
      public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = mRecyclerView.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (loading) {
          if (totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
          }
        }
        if (!loading && (totalItemCount - visibleItemCount)
            <= (firstVisibleItem + visibleThreshold)) {

          addToCourseList(true);
        }
      }
    });
  }

  private boolean searchMode() {
    return courseSearchView != null && courseSearchView.getQuery().length() > 0;
  }

  private void addToCourseList(final boolean append) {

    loading = true;

    if(!append) {
      mRecyclerView.setVisibility(View.INVISIBLE);
      searchingProgressView.setVisibility(View.VISIBLE);
      displayedCourseList.clear();
    }

    if(queryCall != null) {
      queryCall.cancel();
    }

    if(pinMode && searchMode()) {
      queryCall =
          courses.searchPinned(courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else if(pinMode) {
      queryCall =
          courses.listPinned(LatLon.fromLocation(lastLocation), 20, displayedCourseList.size());
    } else if(searchMode()) {
      queryCall =
          courses.search(courseSearchView.getQuery().toString(), 20, displayedCourseList.size());
    } else {
      queryCall = courses.list(LatLon.fromLocation(lastLocation), 20, displayedCourseList.size());
    }

    queryCall.
        onSuccess(new BackendResponse.BackendSuccessListener<CourseSummary[]>() {
          @Override
          public void onSuccess(final CourseSummary[] value) {
            displayedCourseList.addAll(Arrays.asList(value));
          }
        }).
        onFinally(new BackendResponse.BackendFinallyListener() {
          @Override
          public void onFinally() {
            loading = false;
            mRecyclerView.getAdapter().notifyDataSetChanged();
            searchingProgressView.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
          }
        });
  }

}
