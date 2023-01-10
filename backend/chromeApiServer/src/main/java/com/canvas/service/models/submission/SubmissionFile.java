package com.canvas.service.models.submission;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class SubmissionFile {
    private final String name;
    private final String[] fileContent;

    public SubmissionFile(String name, String[] fileContent) {
        this.name = name;
        this.fileContent = Arrays.copyOf(fileContent, fileContent.length);
    }
}
