package org.cranst0n.dogleg.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageButton;

import org.cranst0n.dogleg.android.R;

public class TextlessCheckbox extends ImageButton implements Checkable {

  private boolean checked;

  private OnCheckedChangeListener listener;

  public static interface OnCheckedChangeListener {
    void onCheckedChanged(TextlessCheckbox checkbox, boolean isChecked);
  }

  public TextlessCheckbox(Context context) {
    super(context);
    init();
  }

  public TextlessCheckbox(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TextlessCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {

    setScaleType(ScaleType.CENTER_INSIDE);
    updateState();

    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View view) {

      }
    });
  }

  @Override
  public void setOnClickListener(final OnClickListener l) {
    super.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View view) {
        toggle();
        l.onClick(view);
      }
    });
  }

  @Override
  public void setChecked(final boolean checked) {
    if (checked != this.checked) {
      this.checked = checked;
      updateState();

      if (listener != null) {
        listener.onCheckedChanged(this, this.checked);
      }
    }
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void toggle() {
    setChecked(!isChecked());
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    updateState();
  }

  public void setOnCheckedChangeListener(final OnCheckedChangeListener listener) {
    this.listener = listener;
  }

  private void updateState() {
    if (isEnabled() && isChecked()) {
      setImageResource(R.drawable.checkbox_accent);
    } else if (isChecked()) {
      setImageResource(R.drawable.checkbox_accent_disabled);
    } else {
      setImageResource(R.drawable.checkbox_unchecked);
    }
  }
}
