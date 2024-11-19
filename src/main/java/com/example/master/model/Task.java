package com.example.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Task {
    private UUID taskId;
    private String hash;
    private long startRange;
    private long endRange;
    private boolean isDone;
}
