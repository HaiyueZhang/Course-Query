package edu.illinois.cs.cs125.spring2021.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.cs.cs125.spring2021.mp.R;
import edu.illinois.cs.cs125.spring2021.mp.adapters.viewholder.CourseViewHolder;
import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;

import edu.illinois.cs.cs125.spring2021.mp.databinding.ItemCourseBinding;
import edu.illinois.cs.cs125.spring2021.mp.models.Course;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;
import edu.illinois.cs.cs125.spring2021.mp.network.Client;

/**
 * c.
 */
@SuppressWarnings("checkstyle:WhitespaceAround")
public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {
  private static final String TAG = CourseActivity.class.getSimpleName();

  private final ObjectMapper objectMapper = new ObjectMapper();

  private CourseViewHolder courseViewHolder;

  // Binding to the layout in activity_main.xml
  private ItemCourseBinding binding;


  private TextView title;

  private RatingBar ratingBar;
  private String uuid;
  /**
   * Called when this activity is created.
   *
   * <p>Because this is the main activity for this app, this method is called when the app is
   * started, and any time that this view is shown.
   *
   * @param unused saved instance state, currently unused and always empty or null
   */
  @SuppressWarnings("checkstyle:Indentation")
  @Override
  protected void onCreate(final Bundle unused) {
    super.onCreate(unused);
    Log.i(TAG, "Created");
    Intent intent = getIntent();
    String course = intent.getStringExtra("COURSE");
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    title = (TextView) findViewById(R.id.description);
    ratingBar = (RatingBar) findViewById(R.id.rating);
    CourseableApplication application = (CourseableApplication) getApplication();
    uuid = application.getClientID();
    Summary summary;
    try {
      summary = objectMapper.readValue(course, Summary.class);
      application.getCourseClient().getCourse(summary, this);
      application.getCourseClient().getRating(summary, uuid, this);
      ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
        application.getCourseClient().postRating(summary, new Rating(uuid, rating), this);
      });
      setTitle(summary.getTitle());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    Log.i(TAG, course);
  }

  /**
   * courseResponse.
   * @param summary
   * @param course
   */
  public void courseResponse(final Summary summary, final Course course) {
    //courseViewHolder.performBind(summary);
    title.setText(course.getDescription());
  }

  /**
   * dd.
   * @param summary
   * @param rating
   */
  public void yourRating(final Summary summary, final Rating rating) {
    ratingBar.setRating((float) rating.getRating());
  }
  class RatingBarChangeListener implements RatingBar.OnRatingBarChangeListener {
    @Override
    public void onRatingChanged(final RatingBar ratingBar1, final float rating, final boolean fromUser) {

    }
  }


}

