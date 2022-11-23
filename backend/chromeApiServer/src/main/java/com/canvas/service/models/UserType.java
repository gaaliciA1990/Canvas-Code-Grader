package com.canvas.service.models;

public enum UserType {
    STUDENT,
    GRADER,
    UNAUTHORIZED;

    /**
     * Converts a string to a userType ENUM
     * @param type  string for usertype
     * @return      Enum of UserType
     */
    public static UserType stringToEnum(String type) {
        // convert string to all uppercase letters
        type = type.toUpperCase();

        // Return enum for user type
        return UserType.valueOf(type);
    }
}

