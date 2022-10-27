package com.canvas.chromeApiServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ProcessExecutor {
    private final String[] commands;
    private boolean buildSuccess;
    private final StringBuilder output;

    public ProcessExecutor(String[] commands) {
        this.commands = Arrays.copyOf(commands, commands.length);
        this.buildSuccess = false;
        this.output = new StringBuilder();
    }

    public boolean executeProcess() {
        ProcessBuilder processBuilder = new ProcessBuilder(this.commands);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            process.waitFor();
            this.buildSuccess = process.exitValue() == 0;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = inputReader.readLine()) != null) {
                //System.out.println(line);
                this.output.append(line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return this.buildSuccess;
    }

    public String getProcessOutput() {
        return this.output.toString();
    }
}