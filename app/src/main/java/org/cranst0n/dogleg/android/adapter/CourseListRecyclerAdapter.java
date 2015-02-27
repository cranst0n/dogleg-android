package org.cranst0n.dogleg.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.activity.CourseInfoActivity;
import org.cranst0n.dogleg.android.constants.Photos;
import org.cranst0n.dogleg.android.model.CourseSummary;
import org.cranst0n.dogleg.android.utils.Intents;
import org.cranst0n.dogleg.android.utils.Units;

import java.util.List;

public class CourseListRecyclerAdapter extends RecyclerView.Adapter<CourseListRecyclerAdapter.ViewHolder> {

  private final Activity activity;
  private final List<CourseSummary> courseList;

  public CourseListRecyclerAdapter(final Activity activity, final List<CourseSummary> courseList) {
    this.activity = activity;
    this.courseList = courseList;
  }

  @Override
  public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
    return new ViewHolder(activity,
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_list, parent, false));
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    holder.setCourseSummary(courseList.get(position));
  }

  @Override
  public int getItemCount() {
    return courseList.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    private Activity mActivity;
    private CourseSummary courseSummary;

    public final ImageView courseImageView;
    public final TextView courseNameView;
    public final TextView courseLocationView;
    public final TextView courseNumHolesView;

    public final Button navigationButton;

    public ViewHolder(final Activity activity, final View itemView) {
      super(itemView);

      this.mActivity = activity;

      courseImageView = (ImageView) itemView.findViewById(R.id.course_image);
      courseNameView = (TextView) itemView.findViewById(R.id.course_name);
      courseLocationView = (TextView) itemView.findViewById(R.id.course_city);
      courseNumHolesView = (TextView) itemView.findViewById(R.id.course_num_holes);

      navigationButton = (Button) itemView.findViewById(R.id.course_navigation);

      navigationButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          mActivity.startActivity(Intents.navigationTo(courseSummary.location));
        }
      });

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent i = new Intent(mActivity, CourseInfoActivity.class);
          i.putExtra("courseId", courseSummary.id);
          mActivity.startActivity(i);
        }
      });
    }

    public void setCourseSummary(final CourseSummary courseSummary) {
      this.courseSummary = courseSummary;

      courseImageView.setImageResource(Photos.photoFor((int) courseSummary.id));
      courseNameView.setText(courseSummary.name);
      courseLocationView.setText(courseSummary.city + ", " + courseSummary.state);
      courseNumHolesView.setText(courseSummary.numHoles + " Holes");

      Location lastKnownLocation = DoglegApplication.lastKnownLocation();

      if(lastKnownLocation != null) {
        double miles =
            Units.metersToMiles(lastKnownLocation.distanceTo(courseSummary.location.toLocation()));
        navigationButton.setText(String.format("%.1f miles", miles));
      }
    }
  }
}
