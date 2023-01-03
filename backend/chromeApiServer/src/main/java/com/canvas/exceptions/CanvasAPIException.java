package com.canvas.exceptions;

import java.io.Serial;

/**
 * Handles the failed calls to Canvas' API and displays them using our
 * handler
 */
public class CanvasAPIException extends Exception {
    /**
     * static variable for serial
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that takes in a message and exception to pass to the base exception class
     * overriding the constructor of Exception class
     *
     * @param message string to be displayed
     * @param e       exception thrown
     */
    public CanvasAPIException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor that takes in a message to pass to the base exception class
     * overriding the constructor of Exception class
     *
     * @param message
     */
    public CanvasAPIException(String message) {
        super(message);
    }

    /**
     * Constructor that takes no arg, but still overrides the constructor of Exception class
     */
    public CanvasAPIException() {
        super();
    }

}
