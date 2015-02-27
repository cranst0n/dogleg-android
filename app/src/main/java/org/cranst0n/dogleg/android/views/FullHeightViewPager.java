package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class FullHeightViewPager extends ViewPager {

  public FullHeightViewPager(final Context context) {
    super(context);
  }

  public FullHeightViewPager(final Context context, final AttributeSet attrs) {

    super(context, attrs);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int height = 0;
    for (int i = 0; i < getChildCount(); i++) {

      View child = getChildAt(i);

      child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

      int h = child.getMeasuredHeight();

      if (h > height) height = h;

    }

    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }
}
