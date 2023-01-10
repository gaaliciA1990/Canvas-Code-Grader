package com.canvas.service.models.submission;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Submission {
    private String submissionId;
    private String studentId;
    private String assignmentId;
    private SubmissionFile[] submissionFiles;
    private String submissionDirectory;

    public Submission(
            String submissionId,
            String studentId,
            String assignmentId,
            SubmissionFile[] submissionFiles,
            String submissionDirectory
    ) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.submissionFiles = Arrays.copyOf(submissionFiles, submissionFiles.length);
        this.submissionDirectory = submissionDirectory;
    }
}
