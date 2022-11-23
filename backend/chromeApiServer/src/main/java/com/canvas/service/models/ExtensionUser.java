package com.canvas.service.models;

/**
 * Data class model
 * Representation of the extension user in the domain layer
 *
 * @author Alicia G (agarcia3)
 */
public class ExtensionUser {
    private String bearerToken;
    private String userId;
    private String courseId;
    private String assignmentId;
    private UserType userType;

    private String studentId;

    /**
     * Constructor for creating a new user
     *
     * @param authToken  authorization token from Canvas
     * @param id         canvas user id
     * @param course     canvas course id
     * @param assignment canvas assignment id
     */
    public ExtensionUser(String authToken, String id, String course, String assignment, String studentId, UserType type) {
        this.bearerToken = authToken;
        this.userId = id;
        this.courseId = course;
        this.assignmentId = assignment;
        this.studentId = studentId;
        this.userType = type;

    }


    /**
     * Getter for authorization token
     *
     * @return the auth token
     */
    public String getBearerToken() {
        return bearerToken;
    }

    /**
     * Setter for the authorization token
     *
     * @param bearerToken the authorization token
     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    /**
     * Getter for the userId
     *
     * @return the userId for the given User
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter for the userId from Canvas API
     *
     * @param userId the canvas user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter for the course Id from Canvas
     *
     * @return the canvas course ID
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Setter for the course ID from Canvas
     *
     * @param courseId Canvas course
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Getter for the Assignment id from Canvas
     *
     * @return the canvas assignment
     */
    public String getAssignmentId() {
        return assignmentId;
    }

    /**
     * Setter for the assignment ID from Canvas
     *
     * @param assignmentId Canvas assignment for a course
     */
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * Getter for the student ID
     * @return  String of the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Setter for the student id
     * @param studentId     String of the student ID
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    /**
     * Getter for user type,
     *
     * @return Enum UserType
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Setter for user type
     *
     * @param userType UserType enum
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
