package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.views.switchbutton.Configuration;
import org.cranst0n.dogleg.android.views.switchbutton.SwitchButton;

public class PinSwitch extends SwitchButton {

  public PinSwitch(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public PinSwitch(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PinSwitch(final Context context) {
    super(context);
    init();
  }

  private void init() {
    Configuration switchConfig = Configuration.getDefault(getResources().getDisplayMetrics().density);
    switchConfig.setOffColor(getResources().getColor(R.color.primary_dark));
    switchConfig.setOnColor(Color.parseColor("#dddddd"));
    switchConfig.setThumbWidthAndHeight(42, 42);
    switchConfig.setThumbMargin(-14);
    switchConfig.setMeasureFactor(1.4f);
    switchConfig.setThumbDrawable(getResources().getDrawable(R.drawable.pin_thumb));

    setConfiguration(switchConfig);
  }
}
