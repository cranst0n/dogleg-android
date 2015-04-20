package org.cranst0n.dogleg.android.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import org.cranst0n.dogleg.android.DoglegApplication;
import org.cranst0n.dogleg.android.model.Course;
import org.cranst0n.dogleg.android.model.CourseRequest;
import org.cranst0n.dogleg.android.model.CourseSummary;
import org.cranst0n.dogleg.android.model.LatLon;
import org.cranst0n.dogleg.android.utils.Files;
import org.cranst0n.dogleg.android.utils.Json;
import org.cranst0n.dogleg.android.utils.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Courses extends BackendComponent {

  private static final String Tag = Courses.class.getSimpleName();

  private static final String INFO_URL = "/courses/%d";
  private static final String LIST_URL = "/courses?num=%d&offset=%d";
  private static final String LIST_BY_LOCATION_URL = "/courses?lat=%.5f&lon=%.5f&num=%d&offset=%d";
  private static final String SEARCH_URL = "/courses/search?searchText=%s&num=%d&offset=%d";

  private static final String COURSE_REQUEST_URL = "/courserequests";

  private static final Map<Long, Course> pinnedCache = initPinCache();
  private static final Map<Long, Course> courseCache = new LinkedHashMap<Long, Course>() {
    @Override
    protected boolean removeEldestEntry(final Entry<Long, Course> eldest) {
      return courseCache.size() > 10;
    }
  };

  public Courses(@NonNull final Context context) {
    super(context);
  }

  @NonNull
  public BackendResponse<JsonObject, CourseRequest> requestCourse(
      @NonNull final CourseRequest request) {

    return new BackendResponse<>(Ion.with(context)
        .load("POST", serverUrl(COURSE_REQUEST_URL))
        .setHeader(AuthTokenHeader, authToken())
        .setJsonObjectBody(Json.pimpedGson().toJsonTree(request).getAsJsonObject())
        .asJsonObject()
        .withResponse(), CourseRequest.class);
  }

  @NonNull
  public BackendResponse<JsonObject, Course> info(final long id) {
    if (courseCache.containsKey(id)) {
      return BackendResponse.pure(courseCache.get(id));
    } else if (isPinned(id)) {
      return BackendResponse.fromCallable(new Callable<Course>() {
        @Override
        @Nullable
        public Course call() throws Exception {
          return loadFile(pinFile(id));
        }
      });
    } else {
      return backendCourseInfo(id);
    }
  }

  @NonNull
  private BackendResponse<JsonObject, Course> backendCourseInfo(final long id) {
    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl(String.format(INFO_URL, id)))
        .asJsonObject()
        .withResponse(), Course.class).
        onSuccess(new BackendResponse.BackendSuccessListener<Course>() {
          @Override
          public void onSuccess(@NonNull final Course value) {
            courseCache.put(value.id, value);
          }
        });
  }

  @NonNull
  public BackendResponse<JsonArray, List<CourseSummary>> list(@Nullable final LatLon location,
                                                              final int num, final int offset) {

    String url = serverUrl(
        location == null ?
            String.format(LIST_URL, num, offset) :
            String.format(LIST_BY_LOCATION_URL, location.latitude, location.longitude, num, offset)
    );

    return new BackendResponse<>(Ion.with(context)
        .load(url)
        .asJsonArray()
        .withResponse(), new TypeToken<List<CourseSummary>>() {
    }.getType());
  }

  @NonNull
  public BackendResponse<JsonArray, List<CourseSummary>> listPinned(@Nullable final LatLon location,
                                                                    final int num,
                                                                    final int offset) {

    return BackendResponse.fromCallable(new Callable<List<CourseSummary>>() {
      @Override
      public List<CourseSummary> call() throws Exception {

        List<CourseSummary> pinned = pinnedSummaries();

        if (pinned.size() < offset) {
          return new ArrayList<>();
        } else {

          List<CourseSummary> summaries =
              pinned.subList(offset, Math.min(offset + num, pinned.size()));

          Collections.sort(summaries, new Comparator<CourseSummary>() {
            @Override
            public int compare(final CourseSummary lhs, final CourseSummary rhs) {
              if (location != null) {
                return (int) (location.distanceTo(lhs.location) - location.distanceTo(rhs.location));
              } else {
                return (int) (lhs.id - rhs.id);
              }
            }
          });

          return summaries;
        }
      }
    });
  }

  @NonNull
  public BackendResponse<JsonArray, List<CourseSummary>> search(@NonNull final String query,
                                                                final int num,
                                                                final int offset) {

    return new BackendResponse<>(Ion.with(context)
        .load(serverUrl() + String.format(SEARCH_URL, query, num, offset))
        .asJsonArray()
        .withResponse(), new TypeToken<List<CourseSummary>>() {
    }.getType());
  }

  @NonNull
  public BackendResponse<JsonArray, List<CourseSummary>> searchPinned(@NonNull final String query,
                                                                      final int num,
                                                                      final int offset) {

    return BackendResponse.fromCallable(new Callable<List<CourseSummary>>() {
      @Override
      public List<CourseSummary> call() throws Exception {

        List<CourseSummary> pinned = pinnedSummaries();

        if (pinned.size() < offset) {
          return new ArrayList<>();
        } else {

          List<CourseSummary> summaries =
              pinned.subList(offset, Math.min(offset + num, pinned.size()));

          Collections.sort(summaries, new Comparator<CourseSummary>() {
            @Override
            public int compare(final CourseSummary lhs, final CourseSummary rhs) {
              return Strings.levensteinDistance(query, lhs.name) -
                  Strings.levensteinDistance(query, rhs.name);
            }
          });

          return summaries;
        }
      }
    });
  }

  public boolean pin(@NonNull final Course course) {
    if (!isPinned(course)) {

      pinnedCache.put(course.id, course);

      File file = pinFile(course.id);
      file.getParentFile().mkdirs();
      FileOutputStream outputStream = null;

      try {

        outputStream = new FileOutputStream(file);
        outputStream.write(Json.pimpedGson().toJson(course).getBytes());

        return true;
      } catch (final Exception e) {
        Log.e(Tag, "Failed to write to output stream", e);
        return false;
      } finally {
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (final IOException e) {
            Log.d(Tag, "Failed to close output stream", e);
          }
        }
      }
    }

    return true;
  }

  public boolean unpin(@NonNull final Course course) {
    if (isPinned(course)) {
      pinnedCache.remove(course.id);
      return pinFile(course.id).delete();
    }

    return true;
  }

  private File pinFile(final long id) {
    return new File(context.getFilesDir(), pinPath(id));
  }

  private String pinPath(final long id) {
    return String.format("courses/%d.json", id);
  }

  public boolean isPinned(@NonNull final Course course) {
    return isPinned(course.id);
  }

  public boolean isPinned(final long id) {
    return pinFile(id).exists();
  }

  @NonNull
  private List<CourseSummary> pinnedSummaries() {

    List<CourseSummary> summaries = new ArrayList<>();

    for (Course course : pinnedCache.values()) {
      summaries.add(course.summary());
    }

    return summaries;
  }

  @NonNull
  private static Map<Long, Course> initPinCache() {

    Map<Long, Course> cache = new HashMap<>();

    File pinnedDir = new File(DoglegApplication.application().context().getFilesDir(), "courses/");
    pinnedDir.mkdirs();

    File[] courseFiles = pinnedDir.listFiles();

    for (final File f : courseFiles) {

      Course c = loadFile(f);

      if (c != null) {
        cache.put(c.id, c);
      }
    }

    return cache;
  }

  @Nullable
  private static Course loadFile(@NonNull final File f) {

    try {

      String jsonString = Files.getStringFromFile(f);
      return Json.pimpedGson().fromJson(jsonString, Course.class);

    } catch (final IOException e) {
      Log.e(Tag, "Failed to read course file: " + f.getName(), e);
    }

    return null;
  }
}
