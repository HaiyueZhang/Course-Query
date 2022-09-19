package edu.illinois.cs.cs125.spring2021.mp.models;

/**
 * Course.
 */
public class Course extends Summary {
  private String description;
  private String year;
  private String semester;
  private String department;
  private String number;
  private String title;

    /**
     * description.
     * @return description
     */
  public String getDescription() {

    return description;
  }

    /**
     * set.
     * @param setDescription
     */
  public void setDescription(final String setDescription) {
    description = setDescription;
  }

    /**
     * getYear.
     * @return year
     */
  public String getYear() {
    return year;
  }

    /**
     * dd.
     * @param setYear
     */
  public void setYear(final String setYear) {
    year = setYear;
  }

    /**
     * getSemester.
     * @return semester
     */
  public String getSemester() {
    return semester;
  }

    /**
     * dd.
     * @param setSemester
     */
  public void setSemester(final String setSemester) {
    semester = setSemester;
  }

    /**
     * getDepartment.
     * @return department
     */
  public String getDepartment() {
    return department;
  }

  /**
   * dd.
   * @param setDepartment
   */
  public void setDepartment(final String setDepartment) {
    department = setDepartment;
  }

    /**
     * getNumber.
     * @return number
     */

  public String getNumber() {
    return number;
  }

  /**
   * dd.
   * @param setNumber
   */
  public void setNumber(final String setNumber) {
    number = setNumber;
  }

    /**
     * getTitle.
     * @return title
     */
  public String getTitle() {
    return title;
  }

  /**
   * dd.
   * @param setTitle
   */
  public void setTitle(final String setTitle) {
    title = setTitle;
  }
}
