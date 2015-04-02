package org.cranst0n.dogleg.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.fragment.DrawerFragment;

import java.util.List;

public class DrawerMenuAdapter extends BaseAdapter {

  private final Context context;
  private final List<DrawerFragment.DrawerMenuItem> menuItems;

  public DrawerMenuAdapter(final Context context, final List<DrawerFragment.DrawerMenuItem> listItems) {
    this.context = context;
    this.menuItems = listItems;
  }

  @Override
  public int getCount() {
    return menuItems.size();
  }

  @Override
  public Object getItem(final int position) {
    return menuItems.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  @Override
  public View getView(final int position, View convertView, final ViewGroup parent) {

    ViewHolder holder;

    if (convertView == null) {

      LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      convertView = mInflater.inflate(R.layout.fragment_drawer_menu_item, null);

      ImageView iconView = (ImageView) convertView.findViewById(R.id.fragment_drawerMenu_comp_icon);
      TextView titleView = (TextView) convertView.findViewById(R.id.fragment_drawerMenu_comp_title);
      holder = new ViewHolder(iconView, titleView);

      convertView.setTag(holder);

    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    DrawerFragment.DrawerMenuItem item = menuItems.get(position);

    holder.iconView.setImageDrawable(DoglegApplication.context().getResources().getDrawable(item.iconRes));
    holder.titleView.setText(item.title);
    holder.menuItem = item;

    return convertView;
  }

  public static class ViewHolder {

    public final ImageView iconView;
    public final TextView titleView;

    public DrawerFragment.DrawerMenuItem menuItem;

    private ViewHolder(final ImageView iconView, final TextView titleView) {
      this.iconView = iconView;
      this.titleView = titleView;
    }
  }

}