package com.canvas.service.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class handles the output messages
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommandOutput {
    private boolean success;
    private String[] output;
}