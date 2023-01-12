package com.canvas.service.models.submission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
public class Submission {
    private String submissionId;
    private String studentId;
    private String assignmentId;

    @Setter
    private SubmissionFile[] submissionFiles;
    @Setter
    private String submissionDirectory;

    @JsonIgnore
    private Map<String, byte[]> submissionFileBytes;
}
