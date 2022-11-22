package com.canvas.service.models;

import com.canvas.service.models.CommandOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.Assert.assertEquals;

class CommandOutputUnitTest {

    /**
     * Tests the get output class returns our expected string
     */
    @Test
    public void getOutput_returns_test_string_output() {
        // Set up
        String test_output = "test";
        boolean test_success = true;
        CommandOutput output = new CommandOutput(test_success, test_output);

        // Act

        // Assert
        assertEquals(test_output, output.getOutput());
    }

    /**
     * This test ensures the string is updated as expected. Multiple inputs tested
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/setOutput_test_strings.csv", numLinesToSkip = 1)
    public void setOuput_updates_output_string(String output, String expectedResult) {
        // Set up
        CommandOutput commandOutput = new CommandOutput(true, output);

        // Act
        String testResult = commandOutput.getOutput();

        // Assert
        assertEquals(expectedResult, testResult);
    }
    @ParameterizedTest
    @CsvFileSource(resources = "/isSuccsess_bools.csv", numLinesToSkip = 1)
    public void isSuccess_returns_boolean_that_is_passed(boolean success, boolean expectedResult) {
        // Set up
        String output = "test String";
        CommandOutput commandOutput = new CommandOutput(success, output);

        // Act

        // Assert
        assertEquals(expectedResult, commandOutput.isSuccess());
    }

    @Test
    public void setSuccess_correctly_sets_success_boolean() {
        // Set up
        String output = "test String";
        CommandOutput commandOutput = new CommandOutput(false, output);

        boolean success = true;

        // Act
        commandOutput.setSuccess(success);
        boolean testResult = commandOutput.isSuccess();

        // Assert
        assertEquals(true, testResult);
    }

}