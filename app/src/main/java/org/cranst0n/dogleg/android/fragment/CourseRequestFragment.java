package org.cranst0n.dogleg.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.cranst0n.dogleg.android.R;
import org.cranst0n.dogleg.android.backend.BackendResponse;
import org.cranst0n.dogleg.android.backend.Courses;
import org.cranst0n.dogleg.android.model.CourseRequest;
import org.cranst0n.dogleg.android.model.User;
import org.cranst0n.dogleg.android.utils.BusProvider;
import org.cranst0n.dogleg.android.utils.SnackBars;

public class CourseRequestFragment extends BaseFragment {

  private Bus bus;

  private Courses courses;

  private View requestView;

  private TextView requestNameView;
  private TextView requestCityView;
  private TextView requestStateView;
  private TextView requestCountryView;
  private TextView requestWebsiteView;
  private TextView requestCommentView;
  private Button requestCourseButton;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bus = BusProvider.Instance.bus;
    bus.register(this);

    courses = new Courses(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    bus.unregister(this);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

    requestView = inflater.inflate(R.layout.fragment_course_request, container, false);

    requestNameView = (TextView) requestView.findViewById(R.id.course_request_name);
    requestCityView = (TextView) requestView.findViewById(R.id.course_request_city);
    requestStateView = (TextView) requestView.findViewById(R.id.course_request_state);
    requestCountryView = (TextView) requestView.findViewById(R.id.course_request_country);
    requestWebsiteView = (TextView) requestView.findViewById(R.id.course_request_website);
    requestCommentView = (TextView) requestView.findViewById(R.id.course_request_comment);
    requestCourseButton = (Button) requestView.findViewById(R.id.request_course_button);

    requestCourseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        requestCourse();
      }
    });

    return requestView;
  }

  @Subscribe
  public void newUser(final User user) {
    if(!user.isValid()) {
      activity.finish();
    }
  }

  private void requestCourse() {
    if(validateForm()) {
      String courseName = requestNameView.getText().toString();
      String courseCity = requestCityView.getText().toString();
      String courseState = requestStateView.getText().toString();
      String courseCountry = requestCountryView.getText().toString();
      String courseWebsite = requestWebsiteView.getText().toString();
      String courseComment = requestCommentView.getText().toString();

      courses.requestCourse(new CourseRequest(courseName, courseCity, courseState, courseCountry,
          courseWebsite, courseComment))
          .onSuccess(new BackendResponse.BackendSuccessListener<CourseRequest>() {
            @Override
            public void onSuccess(final CourseRequest value) {
              SnackBars.showSimple(activity, "Request submitted");
              activity.finish();
            }
          })
          .onError(SnackBars.showBackendError(activity))
          .onException(SnackBars.showBackendException(activity));
    }
  }

  private boolean validateForm() {

    if(requestNameView.getText().toString().trim().isEmpty()) {
      requestNameView.setError("Course Name Required");
      return false;
    }

    if(requestCityView.getText().toString().trim().isEmpty()) {
      requestCityView.setError("Course City Required");
      return false;
    }

    if(requestStateView.getText().toString().trim().isEmpty()) {
      requestStateView.setError("Course State Required");
      return false;
    }

    if(requestCountryView.getText().toString().trim().isEmpty()) {
      requestCountryView.setError("Course Country Required");
      return false;
    }

    return true;
  }
}
