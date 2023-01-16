package com.canvas.service.models.submission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Submission file model to be included as part of the Submission model.
 */
@Getter
@Setter
@NoArgsConstructor // for testing
@AllArgsConstructor // for testing
public class SubmissionFile {
    private String name;
    private String[] fileContent;
}
