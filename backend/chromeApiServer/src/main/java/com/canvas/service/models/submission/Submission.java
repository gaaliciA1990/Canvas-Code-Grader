package com.canvas.service.models.submission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Student submission model for the JSON response.
 */
@Builder
@Getter
public class Submission {
    private String submissionId;
    private String studentId;
    private String assignmentId;

    // Adding setters for these fields so they can be set later
    @Setter
    private SubmissionFile[] submissionFiles;
    @Setter
    private String submissionDirectory;

    // Ignore this field in the response body
    @JsonIgnore
    private Map<String, byte[]> submissionFileBytes;
}
