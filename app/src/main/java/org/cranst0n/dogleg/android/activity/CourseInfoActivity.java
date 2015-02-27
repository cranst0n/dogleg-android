package org.cranst0n.dogleg.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.view.ViewHelper;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.constants.Photos;
import org.cranst0n.dogleg.android.fragment.CourseRatingFragment;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseRating;
import org.cranst0n.dogleg.android.model.HoleSet;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.Intents;
import org.cranst0n.dogleg.android.utils.SnackBarUtils;
import org.cranst0n.dogleg.android.views.PinSwitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseInfoActivity extends ActionBarActivity implements ObservableScrollViewCallbacks {

  private Courses courses;
  private Course course;

  private PinSwitch pinCourseSwitch;
  private boolean pinCourseSwitchVisible = false;

  private ImageView mImageView;
  private ObservableScrollView mScrollView;

  private ProgressBar loadingProgressBar;

  private TextView cityTextView;
  private TextView stateTextView;
  private TextView numHolesTextView;

  private ImageButton navigationButton;
  private Button startRoundButton;

  private CardView courseRatingsCard;
  private PagerSlidingTabStrip ratingsTabStrip;
  private ViewPager ratingsViewPager;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_course_info);

    getSupportActionBar().setElevation(0);

    mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
    mScrollView.setScrollViewCallbacks(this);

    loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progress_bar);

    mImageView = (ImageView)findViewById(R.id.course_image);
    cityTextView = (TextView) findViewById(R.id.course_city);
    stateTextView = (TextView) findViewById(R.id.course_state);
    numHolesTextView = (TextView) findViewById(R.id.course_num_holes);
    navigationButton = (ImageButton) findViewById(R.id.course_navigation);
    startRoundButton = (Button) findViewById(R.id.action_start_round);

    courseRatingsCard = (CardView) findViewById(R.id.course_ratings_card);
    ratingsTabStrip = (PagerSlidingTabStrip) findViewById(R.id.ratings_tab_strip);
    ratingsTabStrip.setTextColor(getResources().getColor(R.color.accent));
    ratingsViewPager = (ViewPager) findViewById(R.id.ratings_view_pager);
    ratingsViewPager.setOffscreenPageLimit(5);

    navigationButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        startActivity(Intents.navigationTo(course.location));
      }
    });

    startRoundButton.setOnClickListener(new PlayRoundClickListener());

    long courseId = (Long) getIntent().getSerializableExtra("courseId");
    mImageView.setImageResource(Photos.photoFor((int) courseId));

    courses = new Courses(this);
    courses.info(courseId).
        onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
          @Override
          public void onSuccess(final Course value) {
            setCourse(value);

            loadingProgressBar.setVisibility(View.INVISIBLE);
            cityTextView.setVisibility(View.VISIBLE);
            stateTextView.setVisibility(View.VISIBLE);
            numHolesTextView.setVisibility(View.VISIBLE);
            navigationButton.setVisibility(View.VISIBLE);
            startRoundButton.setVisibility(View.VISIBLE);
            courseRatingsCard.setVisibility(View.VISIBLE);
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.course_info_activity_menu, menu);

    MenuItem pinItem = menu.findItem(R.id.pinned_switch);
    pinCourseSwitch = (PinSwitch) MenuItemCompat.getActionView(pinItem);

    if(pinItem.isVisible() != pinCourseSwitchVisible) {
      pinItem.setVisible(pinCourseSwitchVisible);
    }

    if(course != null) {
      pinCourseSwitch.setChecked(Courses.isPinned(course));
    }

    pinCourseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        if(isChecked) {
          if(Courses.pin(course)) {
            SnackBarUtils.showSimple(CourseInfoActivity.this, "Pinned course for offline use.");
          } else {
            SnackBarUtils.showSimple(CourseInfoActivity.this, "Failed to pin course.");
          }
        } else {
          if(Courses.unpin(course)) {
            SnackBarUtils.showSimple(CourseInfoActivity.this, "Unpinned course.");
          } else {
            SnackBarUtils.showSimple(CourseInfoActivity.this, "Failed to unpin course.");
          }
        }
      }
    });

    return true;
  }

  private void setCourse(final Course course) {
    this.course = course;

    if(course != null) {
      getSupportActionBar().setTitle(course.name);

      if(pinCourseSwitch != null) {
        pinCourseSwitch.setChecked(Courses.isPinned(course));
      }

      pinCourseSwitchVisible = true;
      invalidateOptionsMenu();

      cityTextView.setText(course.city);
      stateTextView.setText(course.state);
      numHolesTextView.setText(course.numHoles + " Holes");

      ratingsViewPager.setAdapter(
          new RatingsPagerAdapter(Arrays.asList(course.ratings), getSupportFragmentManager()));

      ratingsTabStrip.setViewPager(ratingsViewPager);
      ratingsTabStrip.setTextColor(getResources().getColor(R.color.primary));

    }
  }

  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
  }

  @Override
  public void onScrollChanged(final int scrollY, final boolean firstScroll, final boolean dragging) {
    ViewHelper.setTranslationY(mImageView, scrollY / 2);
  }

  @Override
  public void onDownMotionEvent() {

  }

  @Override
  public void onUpOrCancelMotionEvent(final ScrollState scrollState) {

  }

  private class RatingsPagerAdapter extends FragmentPagerAdapter {

    private final List<CourseRating> ratings;
    private final List<CourseRatingFragment> fragments;

    public RatingsPagerAdapter(final List<CourseRating> ratings, final FragmentManager childFragmentManager) {
      super(childFragmentManager);
      this.ratings = ratings;
      this.fragments = new ArrayList<>();

      for(int ix = 0; ix < ratings.size(); ix++) {
        CourseRatingFragment fri = new CourseRatingFragment();
        fri.setRating(course.ratings[ix]);
        fragments.add(fri);
      }
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
      return fragments.get(position);
    }
  }

  private class PlayRoundClickListener implements View.OnClickListener {

    private User user;
    private CourseRating selectedRating;
    private HoleSet selectedHoleSet;
    private boolean official = true;
    private boolean isHandicapOverridden = false;
    private int handicapOverride = 0;

    @Override
    public void onClick(final View v) {
      init();

      if(user.equals(User.NO_USER)) {
        new MaterialDialog.Builder(CourseInfoActivity.this)
            .content("You won't be able to save this round until you login.")
            .positiveText("Ok")
            .callback(new MaterialDialog.ButtonCallback() {
              @Override
              public void onPositive(MaterialDialog dialog) {
                showRatingSelectionDialog();
              }
            })
            .show();
      } else {
        showRatingSelectionDialog();
      }
    }

    private void init() {
      user = User.NO_USER;
      selectedRating = null;
      selectedHoleSet = null;
      official = true;
      isHandicapOverridden = false;
      handicapOverride = 0;
    }

    private void showRatingSelectionDialog() {
      final String[] ratingNames = new String[course.ratings.length];

      for(int ix = 0; ix < course.ratings.length; ix++) {
        ratingNames[ix] = course.ratings[ix].teeName;
      }

      new MaterialDialog.Builder(CourseInfoActivity.this)
          .title("Select Tees")
          .items(ratingNames)
          .itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
              selectedRating = course.ratings[which];
              showHolesSelectionDialog();
            }
          })
          .show();
    }

    private void showHolesSelectionDialog() {
      String[] holeSetNames = new String[HoleSet.values().length];

      for(int ix = 0; ix < HoleSet.values().length; ix++) {
        holeSetNames[ix] = HoleSet.values()[ix].title;
      }

      new MaterialDialog.Builder(CourseInfoActivity.this)
          .title("Select Holes")
          .items(holeSetNames)
          .itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(final MaterialDialog dialog, final View view, final int which, final CharSequence text) {
              selectedHoleSet = HoleSet.values()[which];
              showOfficialRoundDialog();
            }
          })
          .show();
    }

    private void showOfficialRoundDialog() {

      new MaterialDialog.Builder(CourseInfoActivity.this)
          .title("Official Round")
          .content("Should this round be counted towards your handicap?")
          .positiveText("Yes")
          .negativeText("No")
          .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(final MaterialDialog dialog) {
              official = true;
              showHandicapOverrideDialog();
            }

            @Override
            public void onNegative(final MaterialDialog dialog) {
              official = false;
              finished();
            }
          })
          .show();
    }

    private void showHandicapOverrideDialog() {

      new MaterialDialog.Builder(CourseInfoActivity.this)
          .title("Handicap Override")
          .content("Would you like to set your handicap for this round?")
          .positiveText("Yes")
          .negativeText("No")
          .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(final MaterialDialog dialog) {
              isHandicapOverridden = true;
              showHandicapSelectionDialog();
            }

            @Override
            public void onNegative(final MaterialDialog dialog) {
              isHandicapOverridden = false;
              finished();
            }
          })
          .show();
    }

    private void showHandicapSelectionDialog() {

      String[] handicapsOptions = new String[31];

      for(int ix = 0; ix < handicapsOptions.length; ix++) {
        handicapsOptions[ix] = String.valueOf(ix);
      }

      new MaterialDialog.Builder(CourseInfoActivity.this)
          .title("Select Handicap")
          .items(handicapsOptions)
          .itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
              handicapOverride = which;
              finished();
            }
          })
          .show();
    }

    private void finished() {

      Round round = new Round(user, course, selectedRating, System.currentTimeMillis(),
      official, 0, isHandicapOverridden, handicapOverride);

      SnackBarUtils.showSimpleMultiline(
          CourseInfoActivity.this, "Playing the " + selectedHoleSet.title +
              " from the " + selectedRating.teeName + " tees. Handicap: " + handicapOverride);
    }
  };
}
