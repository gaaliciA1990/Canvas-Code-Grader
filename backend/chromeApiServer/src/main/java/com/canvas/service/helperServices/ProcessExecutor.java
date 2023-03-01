package com.canvas.service.helperServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TODO: What is this classes responsibility?
 */
public class ProcessExecutor {
    private final String[] commands;
    private boolean buildSuccess;
    private final List<String> output;
    private final String directory;

    /**
     * Constructor for list of commands
     *
     * @param commands  list of commands to execute
     * @param directory directory name to generate
     */
    public ProcessExecutor(List<String> commands, String directory) {
        this(commands.toArray(new String[0]), directory);
    }

    /**
     * Constructor for array of commands
     *
     * @param executeCommands array of commands to execute
     * @param directory       directory name to generate
     */
    public ProcessExecutor(String[] executeCommands, String directory) {
        this.commands = Arrays.copyOf(executeCommands, executeCommands.length);
        this.directory = directory;
        this.buildSuccess = false;
        this.output = new ArrayList<>();
    }

    /**
     * Executes the process using the commands passed in the constructor
     *
     * @return whether the process was successful or not
     */
    public boolean executeProcess() {
        ProcessBuilder processBuilder = new ProcessBuilder(this.commands);
        processBuilder.directory(new File(this.directory));
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            process.waitFor(5, TimeUnit.SECONDS);
            this.buildSuccess = process.exitValue() == 0;
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = inputReader.readLine()) != null) {
                this.output.add(line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return this.buildSuccess;
    }

    /**
     * Getter for the output of the program
     *
     * @return string of the process output
     */
    public String[] getProcessOutput() {
        return this.output.toArray(new String[0]);
    }
}