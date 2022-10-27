package com.canvas.dto;

public class CommandOutput {
    private boolean success;
    private String output;

    public CommandOutput(boolean success, String output) {
        this.success = success;
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}