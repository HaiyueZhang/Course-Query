package edu.illinois.cs.cs125.spring2021.mp.network;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
@SuppressWarnings("checkstyle:LineLength")
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private MockResponse getSummary(@NonNull final String path) throws JsonProcessingException {
    //  "/summary/""2021/smester"
    String[] parts = path.split("/");
    if (parts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = summaries.get(parts[0] + "_" + parts[1]);
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  private MockResponse getCourse(@NonNull final String path) throws JsonProcessingException {
    String[] parts = path.split("/");
    if (parts.length != 2 + 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = courses.get(new Summary(parts[0], parts[1], parts[2], parts[3], null));
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  /**
   * GET requests to rating/YEAR/SEMESTER/DEPARTMENT/NUMBER?client=UUID should
   * return a Rating for that course by that client.
   * @param path hhh
   * @return hhh
   * @throws JsonProcessingException
   * 2021/spring/CS/463?client=cd9e9446-7c06-4c55-ac07-b57ba07edef9
   * 2021
   * spring
   * CS
   * 463
   * cd9e9446-7c06-4c55-ac07-b57ba07edef9
   */
  private MockResponse getRate(@NonNull final String path) throws JsonProcessingException {
    String[] parts = path.split("/");
    if (parts.length != 2 + 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String[] parts2 = parts[3].split("\\?");
    if (parts2.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String courseNum = parts2[0];
    String[] uuidArr = parts2[1].split("=");
    if (uuidArr.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String uuid = uuidArr[1];
    try {
      UUID.fromString(uuid).toString();
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String summary = courses.get(new Summary(parts[0], parts[1], parts[2], courseNum, null));
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    Rating rating = rates.get(uuid + parts[0] + parts[1] + parts[2] + courseNum);
    if (rating == null) {
      // 假如rates中没有当前用户对当前课程的打分
      Rating newRating = new Rating(uuid, Rating.NOT_RATED);
      rates.put(uuid + parts[0] + parts[1] + parts[2] + courseNum, newRating);
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).
              setBody(MAPPER.writeValueAsString(newRating));
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).
            setBody(MAPPER.writeValueAsString(rating));
  }

  /**
   * dd.
   * @param path ddd.
   * @param requestBody fff.
   * @return dd.
   * @throws JsonProcessingException dd.
   */
  private MockResponse postRate(@NonNull final String path, @NonNull final  String requestBody)
          throws JsonProcessingException {
    String[] parts = path.split("/");
    if (parts.length != 2 + 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String[] parts2 = parts[3].split("\\?");
    if (parts2.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String courseNum = parts2[0];
    String[] uuidArr = parts2[1].split("=");
    if (uuidArr.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String uuid = uuidArr[1];
    try {
      UUID.fromString(uuid).toString();
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Rating requestRating;
    try {
      requestRating = MAPPER.readValue(requestBody, Rating.class);
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    if (!uuid.equals(requestRating.getId())) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = courses.get(new Summary(parts[0], parts[1], parts[2], courseNum, null));
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }

    rates.put(uuid + parts[0] + parts[1] + parts[2] + courseNum, requestRating);

    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(MAPPER.writeValueAsString(requestRating));
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>();
  private final Map<String, Rating> rates = new HashMap<>();
  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {

    try {
      String path = request.getPath();
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("GET")) {
        return new MockResponse().setBody("CS125").setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", ""));
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/")) {
        if (request.getMethod().equalsIgnoreCase("GET")) {
          return getRate(path.replaceFirst("/rating/", ""));
        } else if (request.getMethod().equalsIgnoreCase("POST")) {
          String body = request.getBody().readUtf8();
          return postRate(path.replaceFirst("/rating/", ""), body);
        }
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      e.printStackTrace();
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  /**
   * Start the server if has not already been started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!isRunning(false)) {
      new Thread(Server::new).start();
    }
    if (!isRunning(true)) {
      throw new IllegalStateException("Server should be running");
    }
  }

  /** Number of times to check the server before failing. */
  private static final int RETRY_COUNT = 8;

  /** Delay between retries. */
  private static final int RETRY_DELAY = 512;

  /**
   * Determine if the server is currently running.
   *
   * @param wait whether to wait or not
   * @return whether the server is running or not
   * @throws IllegalStateException if something else is running on our port
   */
  public static boolean isRunning(final boolean wait) {
    for (int i = 0; i < RETRY_COUNT; i++) {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(CourseableApplication.SERVER_URL).get().build();
      try {
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
          if (Objects.requireNonNull(response.body()).string().equals("CS125")) {
            return true;
          } else {
            throw new IllegalStateException(
                    "Another server is running on port " + CourseableApplication.DEFAULT_SERVER_PORT);
          }
        }
      } catch (IOException ignored) {
        if (!wait) {
          break;
        }
        try {
          Thread.sleep(RETRY_DELAY);
        } catch (InterruptedException ignored1) {
        }
      }
    }
    return false;
  }


  private Server() {
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2021", "spring");
    loadCourses("2021", "spring");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.DEFAULT_SERVER_PORT);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
            new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
            new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = MAPPER.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = MAPPER.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
