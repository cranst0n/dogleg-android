package org.cranst0n.dogleg.android.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeLeftRightGestureListener extends GestureDetector.SimpleOnGestureListener {

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private OnSwipeListener onSwipeLeft = null;
  private OnSwipeListener onSwipeRight = null;

  public interface OnSwipeListener {
    void onSwipe();
  }

  public SwipeLeftRightGestureListener onSwipeLeft(final OnSwipeListener listener) {
    onSwipeLeft = listener;
    return this;
  }

  public SwipeLeftRightGestureListener onSwipeRight(final OnSwipeListener listener) {
    onSwipeRight = listener;
    return this;
  }

  public GestureDetector detectOn(final View view, final Context context) {

    final GestureDetector gestureDetector = new GestureDetector(context, this);

    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(final View v, final MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
      }
    });

    return gestureDetector;
  }

  @Override
  public final boolean onFling(final MotionEvent e1, final MotionEvent e2,
                               final float velocityX, final float velocityY) {

    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
      return false;
    // right to left swipe
    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      if (onSwipeRight != null) {
        onSwipeRight.onSwipe();
      }
    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      if (onSwipeLeft != null) {
        onSwipeLeft.onSwipe();
      }
    }

    return false;
  }

  @Override
  public final boolean onDown(final MotionEvent e) {
    return true;
  }
}
