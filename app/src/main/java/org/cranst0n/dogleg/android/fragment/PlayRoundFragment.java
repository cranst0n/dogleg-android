package org.cranst0n.dogleg.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.PlayRoundActivity;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.gesture.SwipeLeftRightGestureListener;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;
import org.cranst0n.dogleg.android.utils.Strings;

public class PlayRoundFragment extends BaseFragment {

  private PlayRoundListener playRoundListener;

  private View playRoundView;

  private ViewPager viewPager;

  private CardView headerCard;
  private TextView currentHoleView;
  private TextView currentHoleSuffixView;
  private TextView currentHoleParView;
  private TextView currentHoleYardageView;
  private TextView currentHoleHandicapView;
  private TextView roundScoreView;
  private TextView roundScoreToParView;

  private ImageButton previousHoleButton;
  private ImageButton nextHoleButton;

  private PlayRoundHoleViewFragment holeViewFragment;
  private PlayRoundScorecardFragment scorecardFragment;
  private PlayRoundMapFragment mapFragment;

  public static PlayRoundFragment instance(final PlayRoundHoleViewFragment holeViewFragment,
                                           final PlayRoundScorecardFragment scorecardFragment,
                                           final PlayRoundMapFragment mapFragment) {

    PlayRoundFragment prf = new PlayRoundFragment();

    prf.holeViewFragment = holeViewFragment;
    prf.scorecardFragment = scorecardFragment;
    prf.mapFragment = mapFragment;

    return prf;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    if (activity instanceof PlayRoundListener) {
      playRoundListener = (PlayRoundListener) activity;
    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    playRoundView = inflater.inflate(R.layout.fragment_play_round, container, false);

    loadViewComponents();
    attachListeners();

    return playRoundView;
  }

  private void loadViewComponents() {

    viewPager = (ViewPager) playRoundView.findViewById(R.id.play_round_view_pager);
    viewPager.setAdapter(new PlayRoundPagerAdapter(getChildFragmentManager()));
    viewPager.setOffscreenPageLimit(2);
    viewPager.setCurrentItem(1);

    headerCard = (CardView) playRoundView.findViewById(R.id.play_round_header_card);
    currentHoleView = (TextView) playRoundView.findViewById(R.id.current_hole);
    currentHoleSuffixView = (TextView) playRoundView.findViewById(R.id.current_hole_suffix);
    currentHoleParView = (TextView) playRoundView.findViewById(R.id.current_hole_par);
    currentHoleYardageView = (TextView) playRoundView.findViewById(R.id.current_hole_yardage);
    currentHoleHandicapView = (TextView) playRoundView.findViewById(R.id.current_hole_handicap);
    roundScoreView = (TextView) playRoundView.findViewById(R.id.round_score);
    roundScoreToParView = (TextView) playRoundView.findViewById(R.id.round_score_to_par);

    previousHoleButton = (ImageButton) playRoundView.findViewById(R.id.previous_hole_button);
    nextHoleButton = (ImageButton) playRoundView.findViewById(R.id.next_hole_button);
  }

  private void attachListeners() {

    new SwipeLeftRightGestureListener().
        onSwipeLeft(new SwipeLeftRightGestureListener.OnSwipeListener() {
          @Override
          public void onSwipe() {
            viewPager.setCurrentItem(Math.max(viewPager.getCurrentItem() - 1, 0));
          }
        }).
        onSwipeRight(new SwipeLeftRightGestureListener.OnSwipeListener() {
          @Override
          public void onSwipe() {
            viewPager.setCurrentItem(
                Math.min(viewPager.getCurrentItem() + 1, viewPager.getAdapter().getCount()));
          }
        }).detectOn(headerCard, context);

    previousHoleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (playRoundListener != null) {
          playRoundListener.previousHole();
        }
      }
    });

    nextHoleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (playRoundListener != null) {
          playRoundListener.nextHole();
        }
      }
    });
  }

  private Round round() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).round();
    }

    return null;
  }

  private HoleRating currentHoleRating() {
    if (activity instanceof PlayRoundActivity) {
      return ((PlayRoundActivity) activity).currentHoleRating();
    }

    return null;
  }

  public void holeUpdated(final int currentHole) {
    if (isAdded()) {
      currentHoleView.setText(String.valueOf(currentHole));
      currentHoleSuffixView.setText(
          Html.fromHtml(String.format("<sup>%s</sup>", Strings.numberSuffix(currentHole))));
      currentHoleParView.setText(String.format("Par %d", currentHoleRating().par));
      currentHoleYardageView.setText(String.format("%d yards", currentHoleRating().yardage));
      currentHoleHandicapView.setText(String.format("HCP #%d", currentHoleRating().handicap));
    }
  }

  public void updateHole(final HoleScore holeScore) {
    if (round() != null) {
      RoundStats stats = round().stats();
      roundScoreView.setText(String.valueOf(stats.score));
      roundScoreToParView.setText(String.format("(%s)", stats.scoreToParString()));
    }
  }

  public interface PlayRoundListener {
    void previousHole();

    void nextHole();

    void gotoHole(final int holeNumber);

    void updateScore(final HoleScore holeScore);

    void showScoreSelectionDialog(final int holeNumber);

    void showPuttsSelectionDialog(final int holeNumber);

    void showPenaltiesSelectionDialog(final int holeNumber);
  }

  private class PlayRoundPagerAdapter extends FragmentStatePagerAdapter {

    public PlayRoundPagerAdapter(final FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return scorecardFragment;
        case 1:
          return holeViewFragment;
        case 2:
          return mapFragment;
      }

      return holeViewFragment;
    }

    @Override
    public int getCount() {
      return 3;
    }
  }
}