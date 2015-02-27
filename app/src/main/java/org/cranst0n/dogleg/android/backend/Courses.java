package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseSummary;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.utils.Files;
import org.cranst0n.dogleg.android.utils.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class Courses extends BackendComponent {

  private static final String Tag = Courses.class.getSimpleName();

  private static final String INFO_URL = "/courses/%d";
  private static final String LIST_URL = "/courses?num=%d&offset=%d";
  private static final String LIST_BY_LOCATION_URL = "/courses?lat=%.5f&lon=%.5f&num=%d&offset=%d";
  private static final String SEARCH_URL = "/courses/search?searchText=%s&num=%d&offset=%d";

  private static Course[] pinnedCache = null;

  public Courses(final Context context) {
    super(context);
  }

  public BackendResponse<JsonObject, Course> info(final long id) {
    if (isPinned(id)) {
      return BackendResponse.fromCallable(new Callable<Course>() {
        @Override
        public Course call() throws Exception {
          return loadFile(pinFile(id));
        }
      });
    } else {
      return new BackendResponse<>(Ion.with(context)
          .load(serverUrl(String.format(INFO_URL, id)))
          .asJsonObject()
          .withResponse(), Course.class);
    }
  }

  public BackendResponse<JsonArray, CourseSummary[]> list(final LatLon location, final int num, final int offset) {
    String url = serverUrl(
        location == null ?
            String.format(LIST_URL, num, offset) :
            String.format(LIST_BY_LOCATION_URL, location.latitude, location.longitude, num, offset)
    );

    return new BackendResponse<>(Ion.with(context)
        .load(url).asJsonArray().withResponse(), CourseSummary[].class);
  }

  public BackendResponse<JsonArray, CourseSummary[]> listPinned(final LatLon location, final int num, final int offset) {

    return BackendResponse.fromCallable(new Callable<CourseSummary[]>() {
      @Override
      public CourseSummary[] call() throws Exception {

        CourseSummary[] pinned = pinnedSummaries();

        if (pinned.length < offset) {
          return new CourseSummary[0];
        } else {

          CourseSummary[] unsorted = Arrays.copyOfRange(pinned, offset, Math.min(offset + num, pinned.length));

          Arrays.sort(unsorted, new Comparator<CourseSummary>() {
            @Override
            public int compare(final CourseSummary lhs, final CourseSummary rhs) {
              if (location != null) {
                return (int) (location.distanceTo(lhs.location) - location.distanceTo(rhs.location));
              } else {
                return (int) (lhs.id - rhs.id);
              }

            }
          });

          return unsorted;
        }
      }
    });
  }

  public BackendResponse<JsonArray, CourseSummary[]> search(final String query, final int num, final int offset) {
    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl() + String.format(SEARCH_URL, query, num, offset))
        .asJsonArray()
        .withResponse(), CourseSummary[].class);
  }

  public BackendResponse<JsonArray, CourseSummary[]> searchPinned(final String query, final int num, final int offset) {

    return BackendResponse.fromCallable(new Callable<CourseSummary[]>() {
      @Override
      public CourseSummary[] call() throws Exception {

        CourseSummary[] pinned = pinnedSummaries();

        if (pinned.length < offset) {
          return new CourseSummary[0];
        } else {

          CourseSummary[] unsorted =
              Arrays.copyOfRange(pinned, offset, Math.min(offset + num, pinned.length));

          Arrays.sort(unsorted, new Comparator<CourseSummary>() {
            @Override
            public int compare(final CourseSummary lhs, final CourseSummary rhs) {
              return Strings.levensteinDistance(query, lhs.name) -
                  Strings.levensteinDistance(query, rhs.name);
            }
          });

          return unsorted;
        }
      }
    });
  }

  public static boolean pin(final Course course) {
    if (!isPinned(course)) {

      pinnedCache = null;

      File file = pinFile(course.id);
      file.getParentFile().mkdirs();
      FileOutputStream outputStream = null;

      try {

        outputStream = new FileOutputStream(file);
        outputStream.write(new GsonBuilder().create().toJson(course).getBytes());

        return true;
      } catch (final Exception e) {
        Log.e(Tag, "Failed to write to output stream", e);
        e.printStackTrace();
        return false;
      } finally {
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            Log.d(Tag, "Failed to close output stream", e);
          }
        }
      }
    }

    return true;
  }

  public static boolean unpin(final Course course) {
    if (isPinned(course)) {
      pinnedCache = null;
      return pinFile(course.id).delete();
    }

    return true;
  }

  private static Context context() {
    return DoglegApplication.context();
  }

  private static File pinFile(final long id) {
    return new File(context().getFilesDir(), pinPath(id));
  }

  private static String pinPath(final long id) {
    return String.format("courses/%d.json", id);
  }

  public static boolean isPinned(final Course course) {
    return isPinned(course.id);
  }

  public static boolean isPinned(final long id) {
    return pinFile(id).exists();
  }

  private static CourseSummary[] pinnedSummaries() {

    Course[] courses = pinned();
    CourseSummary[] summaries = new CourseSummary[courses.length];

    for (int ix = 0; ix < courses.length; ix++) {
      summaries[ix] = courses[ix].summary();
    }

    return summaries;
  }

  private static synchronized Course[] pinned() {
    if (pinnedCache == null) {

      List<Course> courseList = new ArrayList<>();
      File pinnedDir = new File(DoglegApplication.context().getFilesDir(), "courses/");
      pinnedDir.mkdirs();

      File[] courseFiles = pinnedDir.listFiles();

      for (File f : courseFiles) {

        Course c = loadFile(f);

        if (c != null) {
          courseList.add(c);
        }
      }

      pinnedCache = courseList.toArray(new Course[courseList.size()]);
    }

    return pinnedCache;
  }

  private static Course loadFile(final File f) {

    Gson gson = new GsonBuilder().create();

    try {

      String jsonString = Files.getStringFromFile(f);
      return gson.fromJson(jsonString, Course.class);

    } catch (final IOException e) {
      Log.e(Tag, "Failed to read course file: " + f.getName(), e);
    }

    return null;
  }
}
