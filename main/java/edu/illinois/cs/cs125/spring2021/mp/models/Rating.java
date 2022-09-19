package edu.illinois.cs.cs125.spring2021.mp.models;

/**
 * class rating.
 */
public class Rating {
  /**
   * not rated.
   */
  public static final double NOT_RATED = -1.0;

  private String id;
  private double rating;

  /**
   * dd.
   */
  public Rating() {

  }
  /**
   * dd.
   * @param setId
   * @param setRating
   */
  public Rating(final String setId, final double setRating) {
    id = setId;
    rating = setRating;
  }

    /**
     * ddd.
      * @return ddd.
     */
  public String getId() {
    return id;
  }

  /**
   * dd.
   * @param setId
   */
  public void setID(final String setId) {
    id = setId;
  }

    /**
     * ddd.
     * @return rated.
     */
  public double getRating() {
    return rating;
  }

  /**
   * dd.
   * @param setRating
   */
  public void setRating(final double setRating) {
    rating = setRating;
  }
}
