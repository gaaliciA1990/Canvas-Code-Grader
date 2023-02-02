package com.canvas.service.models.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Student deletion model for the JSON response.
 */
@NoArgsConstructor
@Getter
public class Deletion {

    private boolean success;
    private String description;

    @Builder
    public Deletion(boolean success, String description) {
        this.success = success;
        this.description = description;
    }



}
