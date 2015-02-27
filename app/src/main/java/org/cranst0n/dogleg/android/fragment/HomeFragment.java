package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

  private static final String TAG = HomeFragment.class.getSimpleName();

  private Bus bus;

  private View homeView;
  private PagerSlidingTabStrip tabStrip;

  private ViewPager viewPager;
  private HomePagerAdapter viewPagerAdapter;

  private final List<Fragment> visibleFragments = new ArrayList<>();
  private final Fragment courseListPage = new CourseListFragment();
  private final Fragment feedPage = new FeedFragment();
  private final Fragment roundsPage = new RoundListFragment();

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    homeView = inflater.inflate(R.layout.fragment_home, container, false);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    visibleFragments.add(0, courseListPage);

    loadViewComponents();

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
      visibleFragments.add(roundsPage);
      visibleFragments.add(feedPage);
    } else {
      visibleFragments.remove(feedPage);
      visibleFragments.remove(roundsPage);
    }

    if (viewPagerAdapter != null && tabStrip != null) {
      viewPagerAdapter.notifyDataSetChanged();
      tabStrip.notifyDataSetChanged();
    }
  }

  private class HomePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> fragments;

    public HomePagerAdapter(final FragmentManager fm, final List<Fragment> fragments) {
      super(fm);

      this.fragments = fragments;
    }

    @Override
    public Fragment getItem(final int position) {
      return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (fragments != null && position < fragments.size() &&
          fragments.get(position) instanceof TitledFragment) {
        return ((TitledFragment) fragments.get(position)).getTitle();
      } else {
        return String.format("Tab %d", position);
      }
    }

    @Override
    public int getCount() {
      return fragments != null ? fragments.size() : 0;
    }
  }
}
