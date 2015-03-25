package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.view.ViewHelper;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.PlayRoundActivity;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.constants.Photos;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.utils.Intents;
import org.cranst0n.dogleg.android.utils.SnackBars;
import org.cranst0n.dogleg.android.views.PinSwitch;

import java.util.Arrays;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class CourseInfoFragment extends BaseFragment implements ObservableScrollViewCallbacks {

  private View courseInfoView;

  private Courses courses;
  private Course course;

  private MenuItem pinMenuItem;
  private PinSwitch pinCourseSwitch;

  private ImageView courseImageView;
  private ObservableScrollView scrollView;

  private CircularProgressBar loadingIndicator;

  private TextView cityTextView;
  private TextView stateTextView;
  private TextView statsTextView;

  private ImageButton navigationButton;
  private ImageButton callButton;
  private Button startRoundButton;

  private CardView courseRatingsCard;
  private PagerSlidingTabStrip ratingsTabStrip;
  private ViewPager ratingsViewPager;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    setHasOptionsMenu(true);
    setToolbarOverlaid(true);

    setToolbarBackground(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.primary)));

    courseInfoView = inflater.inflate(R.layout.fragment_course_info, container, false);

    scrollView = (ObservableScrollView) courseInfoView.findViewById(R.id.scroll);
    scrollView.setScrollViewCallbacks(this);

    loadingIndicator = (CircularProgressBar) courseInfoView.findViewById(R.id.loading_indicator);

    courseImageView = (ImageView) courseInfoView.findViewById(R.id.course_image);
    cityTextView = (TextView) courseInfoView.findViewById(R.id.course_city);
    stateTextView = (TextView) courseInfoView.findViewById(R.id.course_state);
    statsTextView = (TextView) courseInfoView.findViewById(R.id.course_stats);
    navigationButton = (ImageButton) courseInfoView.findViewById(R.id.course_navigation);
    callButton = (ImageButton) courseInfoView.findViewById(R.id.course_call);
    startRoundButton = (Button) courseInfoView.findViewById(R.id.action_start_round);

    courseRatingsCard = (CardView) courseInfoView.findViewById(R.id.course_ratings_card);
    ratingsTabStrip = (PagerSlidingTabStrip) courseInfoView.findViewById(R.id.ratings_tab_strip);
    ratingsTabStrip.setTextColor(getResources().getColor(R.color.accent));
    ratingsViewPager = (ViewPager) courseInfoView.findViewById(R.id.ratings_view_pager);
    ratingsViewPager.setOffscreenPageLimit(5);

    onScrollChanged(scrollView.getCurrentScrollY(), false, false);

    navigationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        startActivity(Intents.navigationTo(course.location));
      }
    });

    callButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(Intents.call(course.phoneNumber));
      }
    });

    startRoundButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(activity, PlayRoundActivity.class);
        intent.putExtra(getResources().getString(R.string.intent_course_id_key), course.id);
        activity.startActivity(intent);
      }
    });

    long courseId = (Long) activity.getIntent().getSerializableExtra(
        activity.getResources().getString(R.string.intent_course_id_key));

    Ion.with(courseImageView)
        .centerCrop()
        .load("android.resource://" + activity.getPackageName() + "/" + Photos.photoFor((int) courseId));

    courses = new Courses(context);
    courses.info(courseId).
        onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
          @Override
          public void onSuccess(final Course value) {
            setCourse(value);
          }
        });


    return courseInfoView;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      setCourse(new Gson().fromJson(
          savedInstanceState.getString(Course.class.getCanonicalName()), Course.class));
    }
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

    super.onCreateOptionsMenu(menu, inflater);

    inflater.inflate(R.menu.course_info_activity_menu, menu);

    pinMenuItem = menu.findItem(R.id.pinned_switch);
    pinCourseSwitch = (PinSwitch) MenuItemCompat.getActionView(pinMenuItem);

    initPinnedState();

    if (course != null) {
      pinCourseSwitch.setChecked(Courses.isPinned(course));
    }
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(Course.class.getCanonicalName(), new Gson().toJson(course));
  }

  private void setCourse(final Course course) {
    this.course = course;

    if (course != null) {

      setActionBarTitle(course.name);

      initPinnedState();

      cityTextView.setText(course.city);
      stateTextView.setText(course.state);
      statsTextView.setText(String.format("%d Holes - Par %d", course.numHoles, course.par()));

      ratingsViewPager.setAdapter(
          new RatingsPagerAdapter(Arrays.asList(course.ratings), getChildFragmentManager()));

      ratingsTabStrip.setViewPager(ratingsViewPager);
      ratingsTabStrip.setTextColor(getResources().getColor(R.color.primary));

      loadingIndicator.setVisibility(View.INVISIBLE);
      cityTextView.setVisibility(View.VISIBLE);
      stateTextView.setVisibility(View.VISIBLE);
      statsTextView.setVisibility(View.VISIBLE);
      navigationButton.setVisibility(View.VISIBLE);
      callButton.setVisibility(View.VISIBLE);
      startRoundButton.setVisibility(View.VISIBLE);
      courseRatingsCard.setVisibility(View.VISIBLE);
    }
  }

  private void initPinnedState() {
    if (pinCourseSwitch != null && course != null) {

      pinCourseSwitch.setChecked(Courses.isPinned(course));
      pinMenuItem.setVisible(true);

      if (activity != null) {
        activity.invalidateOptionsMenu();
      }

      pinCourseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
          if (isChecked) {
            if (Courses.pin(course)) {
              SnackBars.showSimple(activity, "Pinned course for offline use.");
            } else {
              SnackBars.showSimple(activity, "Failed to pin course.");
            }
          } else {
            if (Courses.unpin(course)) {
              SnackBars.showSimple(activity, "Unpinned course.");
            } else {
              SnackBars.showSimple(activity, "Failed to unpin course.");
            }
          }
        }
      });
    }
  }

  @Override
  public void onScrollChanged(final int scrollY, final boolean firstScroll, final boolean dragging) {
    int baseColor = getResources().getColor(R.color.primary);
    float alpha = 1 - (float) Math.max(0, courseImageView.getHeight() - scrollY) / courseImageView.getHeight();
    setToolbarBackground(ScrollUtils.getColorWithAlpha(alpha, baseColor));
    ViewHelper.setTranslationY(courseImageView, scrollY / 4);
  }

  @Override
  public void onDownMotionEvent() {

  }

  @Override
  public void onUpOrCancelMotionEvent(final ScrollState scrollState) {

  }

  private class RatingsPagerAdapter extends FragmentPagerAdapter {

    private final List<CourseRating> ratings;

    public RatingsPagerAdapter(final List<CourseRating> ratings, final FragmentManager childFragmentManager) {
      super(childFragmentManager);
      this.ratings = ratings;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
      if (ratings == null || ratings.isEmpty()) {
        return "";
      }
      return ratings.get(position).teeName;
    }

    @Override
    public int getCount() {
      if (ratings == null || ratings.isEmpty()) {
        return 0;
      }

      return ratings.size();
    }

    @Override
    public Fragment getItem(final int position) {
      CourseRatingFragment fragment = new CourseRatingFragment();
      fragment.setRating(course.ratings[position]);
      return fragment;
    }
  }

}
