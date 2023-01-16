package com.canvas.service.models.submission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Student submission model for the JSON response.
 */
@AllArgsConstructor // for testing
@Getter
public class Submission {
    private final String submissionId;
    private final String studentId;
    private final String assignmentId;

    // Ignore this field in the response body
    @JsonIgnore
    private final Map<String, byte[]> submissionFileBytes;

    // Adding setters for these fields so they can be set later
    @Setter
    private SubmissionFile[] submissionFiles;
    @Setter
    private String submissionDirectory;

    @Builder
    public Submission(String submissionId, String studentId, String assignmentId, Map<String, byte[]> submissionFileBytes) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.submissionFileBytes = submissionFileBytes;
    }
}
