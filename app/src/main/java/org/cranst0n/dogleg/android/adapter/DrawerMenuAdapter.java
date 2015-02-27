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
import org.cranst0n.dogleg.android.constants.DrawerMenu;

import java.util.ArrayList;

public class DrawerMenuAdapter extends BaseAdapter {

  private final Context mContext;
  private final ArrayList<DrawerMenu.DrawerMenuItem> mListItemsDrawerMenuBean;

  public DrawerMenuAdapter(final Context mContext, final ArrayList<DrawerMenu.DrawerMenuItem> mListItemsDrawer) {
    this.mContext = mContext;
    this.mListItemsDrawerMenuBean = mListItemsDrawer;
  }

  @Override
  public int getCount() {
    return mListItemsDrawerMenuBean.size();
  }

  @Override
  public Object getItem(final int position) {
    return mListItemsDrawerMenuBean.get(position);
  }

  @Override
  public long getItemId(final int position) {
    return position;
  }

  @Override
  public View getView(final int position, View convertView, final ViewGroup parent) {

    ViewHolder holder;

    if (convertView == null) {

      LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      convertView = mInflater.inflate(R.layout.fragment_drawer_menu_item, null);

      holder = new ViewHolder();
      holder.mIcon = (ImageView) convertView.findViewById(R.id.fragment_drawerMenu_comp_icon);
      holder.mTitle = (TextView) convertView.findViewById(R.id.fragment_drawerMenu_comp_title);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    DrawerMenu.DrawerMenuItem item = mListItemsDrawerMenuBean.get(position);
    holder.mIcon.setImageDrawable(DoglegApplication.context().getResources().getDrawable(item.iconRes));
    holder.mTitle.setText(item.title);

    return convertView;
  }

  private static class ViewHolder {
    ImageView mIcon;
    TextView mTitle;
  }

}