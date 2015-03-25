package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.utils.Colors;

import java.util.concurrent.atomic.AtomicInteger;

public class Views {

  private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

  public static int generateViewId() {

    if (Build.VERSION.SDK_INT >= 17) {
      return View.generateViewId();
    } else {
      for (; ; ) {
        final int result = sNextGeneratedId.get();
        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
        int newValue = result + 1;
        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
        if (sNextGeneratedId.compareAndSet(result, newValue)) {
          return result;
        }
      }
    }
  }

  public static void colorizeSearchView(final SearchView searchView, final int colorRes, final Context context) {

    SearchView.SearchAutoComplete searchAutoComplete =
        (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
    ImageView searchHintIcon = (ImageView) searchView.findViewById(R.id.search_mag_icon);
    TintImageView searchButton = (TintImageView) searchView.findViewById(R.id.search_button);
    TintImageView closeButton = (TintImageView) searchView.findViewById(R.id.search_close_btn);

    searchAutoComplete.setTextColor(context.getResources().getColor(colorRes));
    searchHintIcon.setImageDrawable(Colors.colorize(searchHintIcon.getDrawable(), colorRes, context));
    searchButton.setImageDrawable(Colors.colorize(searchButton.getDrawable(), colorRes, context));
    closeButton.setImageDrawable(Colors.colorize(closeButton.getDrawable(), colorRes, context));

    SpannableStringBuilder stopHint = new SpannableStringBuilder("   ");
    Drawable searchIcon = searchButton.getDrawable();
    Float rawTextSize = searchAutoComplete.getTextSize();
    int textSize = (int) (rawTextSize * 1.25);
    searchIcon.setBounds(0, 0, textSize, textSize);
    stopHint.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    searchAutoComplete.setHint(stopHint);
  }

}
