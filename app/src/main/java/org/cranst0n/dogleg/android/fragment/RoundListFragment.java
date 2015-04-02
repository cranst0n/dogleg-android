package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.RoundShowActivity;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.constants.Photos;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;
import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Time;
import org.cranst0n.dogleg.android.views.Views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RoundListFragment extends BaseFragment {

  private View roundListView;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView recyclerView;
  private SmoothProgressBar appendInProgressBar;
  private TextView noRoundsIndicator;

  private Rounds rounds;

  private final List<Round> displayedRoundList = new ArrayList<>();

  private BackendResponse<JsonArray, List<Round>> queryCall;

  private int previousTotal = 0;
  private boolean loading = true;
  private boolean endOfListReached = false;
  private final int visibleThreshold = 5;
  private int firstVisibleItem, visibleItemCount, totalItemCount;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    rounds = new Rounds(context);

    roundListView = inflater.inflate(R.layout.fragment_round_list, container, false);

    swipeRefreshLayout = (SwipeRefreshLayout) roundListView.findViewById(R.id.swipe_refresh_container);
    recyclerView = (RecyclerView) roundListView.findViewById(R.id.round_list_recycler);
    noRoundsIndicator = (TextView) roundListView.findViewById(R.id.round_list_none_indicator);

    swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        addToRoundList(false);
      }
    });

    appendInProgressBar = (SmoothProgressBar) roundListView.findViewById(R.id.append_in_progress_bar);

    initRecyclerView();

    addToRoundList(false);

    return roundListView;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (queryCall != null) {
      queryCall.cancel();
    }
  }

  @Override
  public String getTitle() {
    return "Rounds";
  }

  public void roundUpdated(final Round updatedRound) {
    for (int ix = 0; ix < displayedRoundList.size(); ix++) {
      if (displayedRoundList.get(ix).id == updatedRound.id) {
        displayedRoundList.set(ix, updatedRound);
        recyclerView.getAdapter().notifyDataSetChanged();
        break;
      }
    }
  }

  public void roundDeleted(final Round deletedRound) {
    for (Round r : displayedRoundList) {
      if (r.id == deletedRound.id) {
        displayedRoundList.remove(r);
        recyclerView.getAdapter().notifyDataSetChanged();
        break;
      }
    }
  }

  private void initRecyclerView() {

    final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(layoutManager);

    recyclerView.setAdapter(new RoundListRecyclerAdapter());

    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override
      public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = RoundListFragment.this.recyclerView.getChildCount();
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

          addToRoundList(true);
        }
      }
    });
  }

  private void addToRoundList(final boolean append) {

    loading = true;

    noRoundsIndicator.setVisibility(View.GONE);

    if (append) {
      appendInProgressBar.setVisibility(View.VISIBLE);
    } else {
      recyclerView.setVisibility(View.INVISIBLE);
      displayedRoundList.clear();
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

    queryCall = rounds.list(20, displayedRoundList.size());

    queryCall.
        onSuccess(new BackendResponse.BackendSuccessListener<List<Round>>() {
          @Override
          public void onSuccess(final List<Round> value) {
            endOfListReached = value.isEmpty();
            displayedRoundList.addAll(value);
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

            if (displayedRoundList.isEmpty()) {
              noRoundsIndicator.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private class RoundListRecyclerAdapter extends RecyclerView.Adapter<RoundListRecyclerAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(activity,
          LayoutInflater.from(parent.getContext()).inflate(R.layout.item_round_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final RoundListRecyclerAdapter.ViewHolder holder, final int position) {
      holder.setRound(displayedRoundList.get(position));
    }

    @Override
    public int getItemCount() {
      return displayedRoundList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      private Round round;

      private final Activity activity;

      private final ImageView courseImageView;
      private final TextView grossScoreView;
      private final TextView grossScoreToParView;
      private final TextView courseNameView;
      private final TextView roundTimeView;

      private final ViewPager scorecardViewPager;
      private final List<Fragment> viewPagerFragments = new ArrayList<>();

      private final ImageButton expandDetailsButton;

      private final DateFormat dateFormatter;

      public ViewHolder(final Activity activity, final View itemView) {

        super(itemView);

        this.activity = activity;
        this.dateFormatter = DateFormat.getDateInstance();
        dateFormatter.setTimeZone(TimeZone.getDefault());

        courseImageView = (ImageView) itemView.findViewById(R.id.course_image);
        grossScoreView = (TextView) itemView.findViewById(R.id.round_gross_score_view);
        grossScoreToParView = (TextView) itemView.findViewById(R.id.round_gross_score_to_par);
        courseNameView = (TextView) itemView.findViewById(R.id.round_course_name);
        roundTimeView = (TextView) itemView.findViewById(R.id.round_date);

        courseImageView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(final View view) {
            Intent intent = new Intent(activity, RoundShowActivity.class);
            intent.putExtra(activity.getResources().getString(R.string.intent_round_data_key),
                Json.pimpedGson().toJson(round));
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getParentFragment().startActivityForResult(intent, 0);
          }
        });

        scorecardViewPager = (ViewPager) itemView.findViewById(R.id.item_round_scorecard_view_pager);
        scorecardViewPager.setId(Views.generateViewId());
        scorecardViewPager.setAdapter(new ScorecardPagerAdapter(getChildFragmentManager()));
        scorecardViewPager.setOffscreenPageLimit(2);
        scorecardViewPager.setVisibility(View.GONE);

        // Turn off the recycler view when paging through scorecard(s)
        scorecardViewPager.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(final View v, final MotionEvent event) {
            swipeRefreshLayout.setEnabled(
                event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL
            );
            return false;
          }
        });

        expandDetailsButton = (ImageButton) itemView.findViewById(R.id.expand_details_button);

        expandDetailsButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            if (scorecardViewPager.getVisibility() == View.VISIBLE) {
              setExpanded(false);
            } else {

              for (int ix = 0; ix < recyclerView.getChildCount(); ix++) {
                ((ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(ix))).setExpanded(false);
              }

              setExpanded(true);
            }
          }
        });
      }

      private void setExpanded(final boolean expanded) {
        if (expanded) {
          scorecardViewPager.setVisibility(View.VISIBLE);
          expandDetailsButton.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up_grey);
        } else {
          scorecardViewPager.setVisibility(View.GONE);
          expandDetailsButton.setImageResource(R.drawable.ic_hardware_keyboard_arrow_down_grey);
        }
      }

      private void setRound(final Round round) {

        this.round = round;

        RoundStats roundStats = round.stats();

        grossScoreView.setText(String.valueOf(roundStats.score));
        grossScoreToParView.setText(String.format("(%s)", roundStats.grossScoreToParString()));
        courseNameView.setText(round.course.name);
        roundTimeView.setText(Time.ago(round.time));

        Ion.with(courseImageView)
            .centerCrop()
            .load("android.resource://" + activity.getPackageName() + "/" + Photos.photoFor(round.course.id));

        List<Fragment> newFragments = new ArrayList<>();

        switch (round.holeSet()) {
          case Front9: {
            newFragments.add(ScorecardFront9Fragment.instance(round).setEnabled(false));
            break;
          }
          case Back9: {
            newFragments.add(ScorecardBack9Fragment.instance(round).setEnabled(false));
            break;
          }
          case All: {
            newFragments.add(ScorecardFront9Fragment.instance(round).setEnabled(false));
            newFragments.add(ScorecardBack9Fragment.instance(round).setEnabled(false));
            break;
          }
        }

        viewPagerFragments.clear();
        viewPagerFragments.addAll(newFragments);
        scorecardViewPager.getAdapter().notifyDataSetChanged();
      }

      private class ScorecardPagerAdapter extends FragmentPagerAdapter {

        public ScorecardPagerAdapter(final FragmentManager childFragmentManager) {
          super(childFragmentManager);
        }

        @Override
        public CharSequence getPageTitle(final int position) {
          return String.valueOf(position);
        }

        @Override
        public int getCount() {
          return viewPagerFragments != null ? viewPagerFragments.size() : 0;
        }

        @Override
        public Fragment getItem(final int position) {
          return viewPagerFragments.get(position);
        }
      }
    }
  }
}
