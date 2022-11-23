package com.canvas.service.models;

/**
 * This class handles the output messages
 */
public class CommandOutput {
    private boolean success;
    private String output;

    /**
     * Adding no arg constructor for testing
     */
    public CommandOutput() {

    }

    /**
     * Constructor TODO: add details about the params
     *
     * @param success
     * @param output
     */
    public CommandOutput(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    /**
     * Getter for the string output
     *
     * @return  String of the output message
     */
    public String getOutput() {
        return output;
    }

    /**
     * Setter for the output String
     *
     * @param output    String of the output message
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * Getter for determining is something was successful
     *
     * @return  boolean, true = success, else fail
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter for the Success variable.
     *
     * @param success   Boolean for success (T/F)
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}