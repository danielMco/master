package com.example.master.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Minion {
    private UUID id;
    private String url;
}
