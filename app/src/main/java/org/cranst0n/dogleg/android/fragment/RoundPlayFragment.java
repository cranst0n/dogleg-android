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
import org.cranst0n.dogleg.android.activity.RoundPlayActivity;
import org.cranst0n.dogleg.android.gesture.SwipeLeftRightGestureListener;
import org.cranst0n.dogleg.android.model.HoleRating;
import org.cranst0n.dogleg.android.model.HoleScore;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.RoundStats;
import org.cranst0n.dogleg.android.utils.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoundPlayFragment extends BaseFragment {

  private PlayRoundListener playRoundListener;

  private View playRoundView;

  private ViewPager viewPager;

  private CardView headerCard;
  private TextView currentHoleView;
  private TextView currentHoleSuffixView;
  private TextView currentHoleParView;
  private TextView currentHoleYardageView;
  private TextView currentHoleHandicapView;

  private TextView roundGrossScoreView;
  private TextView roundGrossScoreToParView;
  private TextView netScoreView;
  private TextView netScoreToParView;

  private ImageButton previousHoleButton;
  private ImageButton nextHoleButton;

  private final List<Fragment> viewPagerFragments = new ArrayList<>();

  public static RoundPlayFragment instance(final Fragment... fragments) {

    RoundPlayFragment prf = new RoundPlayFragment();

    prf.viewPagerFragments.addAll(Arrays.asList(fragments));

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

    playRoundView = inflater.inflate(R.layout.fragment_round_play, container, false);

    loadViewComponents();
    attachListeners();

    return playRoundView;
  }

  private void loadViewComponents() {

    viewPager = (ViewPager) playRoundView.findViewById(R.id.play_round_view_pager);
    viewPager.setAdapter(new PlayRoundPagerAdapter(getChildFragmentManager()));
    viewPager.setOffscreenPageLimit(viewPagerFragments.size() - 1);

    for (int ix = 0; ix < viewPagerFragments.size(); ix++) {
      if (viewPagerFragments.get(ix) instanceof RoundPlayHoleViewFragment) {
        viewPager.setCurrentItem(ix);
      }
    }

    headerCard = (CardView) playRoundView.findViewById(R.id.play_round_header_card);
    currentHoleView = (TextView) playRoundView.findViewById(R.id.current_hole);
    currentHoleSuffixView = (TextView) playRoundView.findViewById(R.id.current_hole_suffix);
    currentHoleParView = (TextView) playRoundView.findViewById(R.id.current_hole_par);
    currentHoleYardageView = (TextView) playRoundView.findViewById(R.id.current_hole_yardage);
    currentHoleHandicapView = (TextView) playRoundView.findViewById(R.id.current_hole_handicap);
    roundGrossScoreView = (TextView) playRoundView.findViewById(R.id.round_gross_score);
    roundGrossScoreToParView = (TextView) playRoundView.findViewById(R.id.round_gross_score_to_par);
    netScoreView = (TextView) playRoundView.findViewById(R.id.round_net_score);
    netScoreToParView = (TextView) playRoundView.findViewById(R.id.round_net_score_to_par);

    previousHoleButton = (ImageButton) playRoundView.findViewById(R.id.previous_hole_button);
    nextHoleButton = (ImageButton) playRoundView.findViewById(R.id.next_hole_button);

    if (round() != null) {
      roundUpdated(round());
    }
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
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).round();
    }

    return null;
  }

  private HoleScore currentHoleScore() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentHoleScore();
    }

    return null;
  }

  private HoleRating currentHoleRating() {
    if (activity instanceof RoundPlayActivity) {
      return ((RoundPlayActivity) activity).currentHoleRating();
    }

    return null;
  }

  public void roundUpdated(final Round round) {
    scoreUpdated(currentHoleScore());
  }

  public void holeUpdated(final int currentHole) {
    if (isAdded()) {

      HoleRating currentHoleRating = currentHoleRating();

      currentHoleView.setText(String.valueOf(currentHole));
      currentHoleSuffixView.setText(
          Html.fromHtml(String.format("<sup>%s</sup>", Strings.numberSuffix(currentHole))));
      currentHoleParView.setText(String.format("Par %d", currentHoleRating.par));
      currentHoleYardageView.setText(String.format("%d yards", currentHoleRating.yardage));
      currentHoleHandicapView.setText(String.format("HCP #%d", currentHoleRating.handicap));
    }
  }

  public void scoreUpdated(final HoleScore holeScore) {
    if (round() != null) {
      RoundStats stats = round().stats();
      roundGrossScoreView.setText(String.valueOf(stats.score));
      roundGrossScoreToParView.setText(String.format("(%s)", stats.grossScoreToParString()));
      netScoreView.setText(String.valueOf(stats.netScore));
      netScoreToParView.setText(String.format("(%s)", stats.netScoreToParString()));
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
      return viewPagerFragments.get(position);
    }

    @Override
    public int getCount() {
      return viewPagerFragments.size();
    }
  }
}
