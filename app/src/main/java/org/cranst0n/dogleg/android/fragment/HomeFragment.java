package org.cranst0n.dogleg.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.RoundPlayActivity;
import org.cranst0n.dogleg.android.backend.Rounds;
import org.cranst0n.dogleg.android.model.Round;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.Json;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

  private Bus bus;

  private View homeView;
  private PagerSlidingTabStrip tabStrip;

  private ViewPager viewPager;
  private HomePagerAdapter viewPagerAdapter;

  private final List<Fragment> visibleFragments = new ArrayList<>();
  private final CourseListFragment courseListPage = new CourseListFragment();
  private final RoundListFragment roundsPage = new RoundListFragment();

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                           final Bundle savedInstanceState) {

    homeView = inflater.inflate(R.layout.fragment_home, container, false);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    visibleFragments.add(0, courseListPage);

    loadViewComponents();

    checkForUnfinishedRounds();

    return homeView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    bus.unregister(this);
  }

  private void loadViewComponents() {

    viewPager = (ViewPager) homeView.findViewById(R.id.fragment_home_view_pager);
    viewPagerAdapter = new HomePagerAdapter(getChildFragmentManager(), visibleFragments);
    viewPager.setAdapter(viewPagerAdapter);
    viewPager.setOffscreenPageLimit(2);

    tabStrip = (PagerSlidingTabStrip) homeView.findViewById(R.id.fragment_home_pager_sliding_tab);
    tabStrip.setTextColor(getResources().getColor(android.R.color.white));
    tabStrip.setViewPager(viewPager);
  }

  @Subscribe
  public void newUser(final User user) {

    if (user.isValid()) {
      if (!visibleFragments.contains(roundsPage)) {
        visibleFragments.add(roundsPage);
      }
    } else {

      if (viewPager != null) {
        viewPager.setCurrentItem(0);
      }

      visibleFragments.remove(roundsPage);
    }

    if (viewPagerAdapter != null && tabStrip != null) {
      viewPagerAdapter.notifyDataSetChanged();
      tabStrip.notifyDataSetChanged();
    }
  }

  private class HomePagerAdapter extends FragmentStatePagerAdapter {

    @NonNull
    private final List<Fragment> fragments;

    public HomePagerAdapter(final FragmentManager fm, @NonNull final List<Fragment> fragments) {
      super(fm);

      this.fragments = fragments;
    }

    @Override
    public Fragment getItem(final int position) {
      return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position < fragments.size() && fragments.get(position) instanceof TitledFragment) {
        return ((TitledFragment) fragments.get(position)).getTitle();
      } else {
        return String.format("Tab %d", position);
      }
    }

    @Override
    public int getCount() {
      return fragments.size();
    }
  }

  @Override
  public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (resultCode) {
      case R.integer.round_edit_result: {
        Round r = Json.pimpedGson().fromJson(
            data.getStringExtra(Round.class.getCanonicalName()), Round.class);
        roundsPage.roundUpdated(r);
        break;
      }
      case R.integer.round_delete_result: {
        Round r = Json.pimpedGson().fromJson(
            data.getStringExtra(Round.class.getCanonicalName()), Round.class);
        roundsPage.roundDeleted(r);
        break;
      }
      default: {
        super.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  private void checkForUnfinishedRounds() {

    final Round backedUpRound = new Rounds(context).backedUpRound();

    if (backedUpRound != null) {

      SnackbarManager.show(
          Snackbar.with(activity)
              .text("You have an unfinished round.")
              .actionLabel("Resume")
              .actionColor(getResources().getColor(R.color.warn))
              .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
              .swipeToDismiss(true)
              .actionListener(new ActionClickListener() {
                @Override
                public void onActionClicked(final Snackbar snackbar) {

                  Intent i = new Intent(activity, RoundPlayActivity.class);
                  i.putExtra(Round.class.getCanonicalName(), Json.pimpedGson().toJson
                      (backedUpRound));

                  startActivity(i);
                }
              })
          , activity);
    }
  }
}
