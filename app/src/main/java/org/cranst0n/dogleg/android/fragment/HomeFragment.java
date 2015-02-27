package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.api.BaseFragment;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends BaseFragment {

  private static final String TAG = HomeFragment.class.getSimpleName();

  private View mViewHome;
  private PagerSlidingTabStrip tabStrip;
  private ViewPager viewPager;

  private List<String> pagerFragments = Arrays.asList("Feed", "Courses", "Rounds");

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    mViewHome = inflater.inflate(R.layout.fragment_home, container, false);

    loadViewComponents();

    return mViewHome;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  private void loadViewComponents() {
    tabStrip = (PagerSlidingTabStrip) mViewHome.findViewById(R.id.fragment_home_pager_sliding_tab);

    viewPager = (ViewPager) mViewHome.findViewById(R.id.fragment_home_view_pager);
    viewPager.setAdapter(new HomeFragmentPagerAdapter(pagerFragments, getChildFragmentManager()));

    tabStrip.setViewPager(viewPager);
    tabStrip.setTextColor(getResources().getColor(android.R.color.white));
  }

  private class HomeFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<String> titles;

    public HomeFragmentPagerAdapter(final List<String> listTitleTabs, final FragmentManager childFragmentManager) {
      super(childFragmentManager);
      this.titles = listTitleTabs;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
      if (titles == null || titles.isEmpty()) {
        return "";
      }
      return titles.get(position);
    }

    @Override
    public int getCount() {
      if (titles == null || titles.isEmpty()) {
        return 0;
      }

      return titles.size();
    }

    @Override
    public Fragment getItem(final int position) {
      switch (position) {
        case 0:
          return FeedFragment.newInstance();
        case 1:
          return new CourseListFragment();
        case 2:
          return new RoundListFragment();
        default:
          return FeedFragment.newInstance();
      }
    }
  }
}
