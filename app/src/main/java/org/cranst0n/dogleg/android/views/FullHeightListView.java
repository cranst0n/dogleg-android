package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FullHeightListView extends ListView {

  public FullHeightListView(final Context context) {
    super(context);
  }

  public FullHeightListView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public FullHeightListView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    setListViewHeightBasedOnChildren();
  }

  private void setListViewHeightBasedOnChildren() {

    ListAdapter listAdapter = getAdapter();

    if (listAdapter == null) {
      // pre-condition
      return;
    }

    int totalHeight = getPaddingTop() + getPaddingBottom();

    for (int i = 0; i < listAdapter.getCount(); i++) {
      View listItem = listAdapter.getView(i, null, this);
      if (listItem instanceof ViewGroup) {
        listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      }
      listItem.measure(0, 0);
      totalHeight += listItem.getMeasuredHeight();
    }

    ViewGroup.LayoutParams params = getLayoutParams();
    params.height = totalHeight + (getDividerHeight() * (listAdapter.getCount() - 1));
    setLayoutParams(params);
  }
}
