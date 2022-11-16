package com.canvas.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ProcessExecutor {
    private final String[] commands;
    private boolean buildSuccess;
    private final StringBuilder output;
    private final String directory;

    public ProcessExecutor(List<String> commands, String directory) {
        this(commands.toArray(new String[0]), directory);
    }

    public ProcessExecutor(String[] executeCommands, String directory) {
        this.commands = Arrays.copyOf(executeCommands, executeCommands.length);
        this.directory = directory;
        this.buildSuccess = false;
        this.output = new StringBuilder();
    }

    public boolean executeProcess() {
        ProcessBuilder processBuilder = new ProcessBuilder(this.commands);
        processBuilder.directory(new File(this.directory));
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            process.waitFor();
            this.buildSuccess = process.exitValue() == 0;
            BufferedReader inputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = inputReader.readLine()) != null) {
                this.output.append(line).append('\n');
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