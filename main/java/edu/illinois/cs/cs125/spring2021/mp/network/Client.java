package edu.illinois.cs.cs125.spring2021.mp.network;

import android.util.Log;
import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.models.Course;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;


import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Course API client.
 *
 * <p>You will add functionality to the client as part of MP1 and MP2.
 */
public final class Client {
  private static final String TAG = Client.class.getSimpleName();
  private static final int INITIAL_CONNECTION_RETRY_DELAY = 1000;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Course API client callback interface.
   *
   * <p>Provides a way for the client to pass back information obtained from the course API server.
   */
  public interface CourseClientCallbacks {
    /**
     * Return course summaries for the given year and semester.
     *
     * @param year the year that was retrieved
     * @param semester the semester that was retrieved
     * @param summaries an array of course summaries
     */
    default void summaryResponse(String year, String semester, Summary[] summaries) {}

    /**
     *
     * @param summary
     * @param course
     */
    default void courseResponse(Summary summary, Course course) {}

    /**
     * yourRating.
     * @param summary
     * @param rating
     */
    default void yourRating(Summary summary, Rating rating){}
  }

  /**
   * Retrieve course summaries for a given year and semester.
   *
   * @param year the year to retrieve
   * @param semester the semester to retrieve
   * @param callbacks the callback that will receive the result
   */
  public void getSummary(
          @NonNull final String year,
          @NonNull final String semester,
          @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "summary/" + year + "/" + semester;
    StringRequest summaryRequest =
            new StringRequest(
                    Request.Method.GET,
                    url,
                    response -> {
                      try {
                        Summary[] courses = objectMapper.readValue(response, Summary[].class);
                        callbacks.summaryResponse(year, semester, courses);
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString()));
    requestQueue.add(summaryRequest);
  }

  /**
   *
   * @param summary ff.
   * @param callbacks dd.
   */
  public void getCourse(
          @NonNull final Summary summary,
          @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "course/"
            + summary.getYear() + "/" + summary.getSemester()
            + "/" + summary.getDepartment() + "/" + summary.getNumber();
    StringRequest summaryRequest =
            new StringRequest(
                    Request.Method.GET,
                    url,
                    response -> {
                      try {
                        Course courses = objectMapper.readValue(response, Course.class);
                        callbacks.courseResponse(summary, courses);
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString()));
    requestQueue.add(summaryRequest);
  }

  /**
   *ddd.
   * @param summary
   * @param id
   * @param callbacks
   */
  public void getRating(
          @NonNull final Summary summary,
          @NonNull final String id,
          @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "rating/"
            + summary.getYear() + "/" + summary.getSemester() + "/"
            + summary.getDepartment() + "/" + summary.getNumber() + "?client=" + id;
    StringRequest summaryRequest =
            new StringRequest(
                    Request.Method.GET,
                    url,
                    response -> {
                      try {
                        Rating rating = objectMapper.readValue(response, Rating.class);
                        callbacks.yourRating(summary, rating);
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString()));
    requestQueue.add(summaryRequest);
  }

  /**
   * dd.
   * @param summary
   * @param rating
   * @param callbacks
   */
  public void postRating(
          @NonNull final Summary summary,
          @NonNull final Rating rating,
          @NonNull final CourseClientCallbacks callbacks) {
    String url1 = summary.getYear() + "/" + summary.getSemester() + "/";
    String url2 = summary.getDepartment() + "/" + summary.getNumber() + "?client=" + rating.getId();
    String url = CourseableApplication.SERVER_URL + "rating/" + url1 + url2;

    ObjectNode newRating = MAPPER.createObjectNode();
    newRating.set("id", MAPPER.convertValue(rating.getId(), JsonNode.class));
    newRating.set("rating", MAPPER.convertValue(rating.getRating(), JsonNode.class));
    String requestBody = newRating.toString();
    StringRequest  jsonObjectRequest = new StringRequest(
          Request.Method.POST,
          url,
          new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
              Rating returnRating = null;
              try {
                returnRating = objectMapper.readValue(response, Rating.class);
                callbacks.yourRating(summary, returnRating);
              } catch (JsonProcessingException e) {
                e.printStackTrace();
              }
            }
          },
            error -> Log.e(TAG, error.toString())
    ) {
      @Override
      public String getBodyContentType() {
        return "application/json; charset=utf-8";
      }

      @Override
      public byte[] getBody() throws AuthFailureError {
        try {
          if (requestBody == null) {
            return null;
          } else {
            return requestBody.getBytes("utf-8");
          }
        } catch (UnsupportedEncodingException uee) {
          VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
          return null;
        }
      }

      @Override
      protected Response<String> parseNetworkResponse(final NetworkResponse response) {
        String responseString = "";
        if (response != null) {
          responseString = new String(response.data);
          // can get more details such as response.headers
        }
        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
      }
    };

    requestQueue.add(jsonObjectRequest);
  }

//  /**
//   * dd.
//   * @param summary
//   * @param rating
//   * @param callbacks
//   */
//  public void postRating(
//          @NonNull final Summary summary,
//          @NonNull final Rating rating,
//          @NonNull final CourseClientCallbacks callbacks) {
//    Map<String, String> params = new HashMap<>();
//    params.put("id", rating.getId());
//    params.put("rating", String.valueOf(rating.getRating()));
//    JSONObject paramJsonObject = new JSONObject(params);
//    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//            Request.Method.POST,
//            url,
//            paramJsonObject,
//            response -> {
//              try {
//                //Rating rating = objectMapper.readValue(response, Rating.class);
//                callbacks.yourRating(summary, new Rating(response.getString("id"), response.getDouble("rating")));
//              } catch (Exception e) {
//                e.printStackTrace();
//              }
//            },
//            error -> Log.e(TAG, error.toString())
//    );
//
//    requestQueue.add(jsonObjectRequest);
//  }


  private static Client instance;

  /**
   * Retrieve the course API client. Creates one if it does not already exist.
   *
   * @return the course API client
   */
  public static Client start() {
    if (instance == null) {
      instance = new Client(false);
    }
    return instance;
  }
  /**
   * Retrieve the course API client. Creates one if it does not already exist.
   *
   * @return the course API client
   */
  public static Client startTesting() {
    return new Client(true);
  }
  private static final int MAX_STARTUP_RETRIES = 8;
  private static final int THREAD_POOL_SIZE = 4;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RequestQueue requestQueue;

  /*
   * Set up our client, create the Volley queue, and establish a backend connection.
   */
  private Client(final boolean testing) {
    // Configure the Volley queue used for our network requests
    Cache cache = new NoCache();
    Network network = new BasicNetwork(new HurlStack());
    HttpURLConnection.setFollowRedirects(true);
    if (testing) {
      requestQueue =
              new RequestQueue(
                      cache,
                      network,
                      THREAD_POOL_SIZE,
                      new ExecutorDelivery(Executors.newSingleThreadExecutor()));
    } else {
      requestQueue = new RequestQueue(cache, network);
    }
    // Configure the Jackson object mapper to ignore unknown properties
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Make sure the backend URL is valid
    URL serverURL;
    try {
      serverURL = new URL(CourseableApplication.SERVER_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Bad server URL: " + CourseableApplication.SERVER_URL);
      return;
    }

    // Start a background thread to establish the server connection
    new Thread(
            () -> {
              for (int i = 0; i < MAX_STARTUP_RETRIES; i++) {
                try {
                  // Issue a HEAD request for the root URL
                  HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
                  connection.setRequestMethod("HEAD");
                  connection.connect();
                  connection.disconnect();
                  // Once this succeeds, we can start the Volley queue
                  requestQueue.start();
                  break;
                } catch (Exception e) {
                  Log.e(TAG, e.toString());
                }
                // If the connection fails, delay and then retry
                try {
                  Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY);
                } catch (InterruptedException ignored) {
                }
              }
            })
            .start();
  }
}
