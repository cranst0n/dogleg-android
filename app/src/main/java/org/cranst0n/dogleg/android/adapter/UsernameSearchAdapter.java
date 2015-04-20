package org.cranst0n.dogleg.android.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Users;
import org.cranst0n.dogleg.android.model.User;

import java.util.ArrayList;
import java.util.List;

public class UsernameSearchAdapter extends ArrayAdapter<User> implements Filterable {

  @NonNull
  private final ArrayList<User> userData = new ArrayList<>();

  @NonNull
  private final Users users;

  public UsernameSearchAdapter(@NonNull final Context context) {
    super(context, 0);

    users = new Users(context);
  }

  @Override
  public int getCount() {
    return userData.size();
  }

  @Override
  public User getItem(final int index) {
    return userData.get(index);
  }

  @Override
  public View getView(final int position, @Nullable View convertView, final ViewGroup parent) {

    final ViewHolder vh;

    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1,
          parent, false);
      vh = new ViewHolder(convertView);
      convertView.setTag(vh);
    } else {
      vh = (ViewHolder) convertView.getTag();
    }

    vh.textView.setText(getItem(position).name);

    return convertView;
  }

  @Override
  public Filter getFilter() {

    return new Filter() {
      @Override
      protected FilterResults performFiltering(final CharSequence constraint) {

        FilterResults filterResults = new FilterResults();

        if (constraint != null) {

          users.searchByName(constraint.toString())
              .onSuccess(new BackendResponse.BackendSuccessListener<List<User>>() {
                @Override
                public void onSuccess(@NonNull final List<User> value) {
                  userData.clear();
                  userData.addAll(value);
                }
              });

          // Now assign the values and count to the FilterResults object
          filterResults.values = userData;
          filterResults.count = userData.size();
        }

        return filterResults;
      }

      @Override
      public CharSequence convertResultToString(final Object resultValue) {
        if (resultValue instanceof User) {
          return ((User) resultValue).name;
        } else {
          return super.convertResultToString(resultValue);
        }
      }

      @Override
      protected void publishResults(final CharSequence contraint, final FilterResults results) {
        if (results != null && results.count > 0) {
          notifyDataSetChanged();
        } else {
          notifyDataSetInvalidated();
        }
      }
    };
  }

  private class ViewHolder {

    private final TextView textView;

    private ViewHolder(final View rootView) {
      textView = (TextView) rootView.findViewById(android.R.id.text1);
      textView.setTextColor(getContext().getResources().getColor(android.R.color.black));
    }
  }
}
