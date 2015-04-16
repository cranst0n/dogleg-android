package org.cranst0n.dogleg.android.adapter;

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

      LayoutInflater mInflater = LayoutInflater.from(context);
      convertView = mInflater.inflate(R.layout.item_drawer_menu, null);

      ImageView iconView = (ImageView) convertView.findViewById(R.id.drawer_menu_item_icon);
      TextView titleView = (TextView) convertView.findViewById(R.id.drawer_menu_item_title);
      TextView experimentalBadge = (TextView) convertView.findViewById(R.id
          .drawer_menu_item_experimental_badge);

      holder = new ViewHolder(iconView, titleView, experimentalBadge);

      convertView.setTag(holder);

    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    DrawerFragment.DrawerMenuItem item = menuItems.get(position);

    holder.iconView.setImageDrawable(
        DoglegApplication.context().getResources().getDrawable(item.iconRes));
    holder.titleView.setText(item.title);
    holder.experimentalBadge.setVisibility(item.experimental ? View.VISIBLE : View.GONE);
    holder.menuItem = item;

    return convertView;
  }

  public static class ViewHolder {

    public final ImageView iconView;
    public final TextView titleView;
    public final TextView experimentalBadge;

    public DrawerFragment.DrawerMenuItem menuItem;

    private ViewHolder(final ImageView iconView, final TextView titleView,
                       final TextView experimentalBadge) {

      this.iconView = iconView;
      this.titleView = titleView;
      this.experimentalBadge = experimentalBadge;
    }
  }

}