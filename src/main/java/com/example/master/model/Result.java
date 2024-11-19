package com.example.master.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Result {
    private Task task;
    private Boolean isSuccess;
    private String password;
    private UUID minionId;
}
