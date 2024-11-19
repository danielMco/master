package com.example.master.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Getter
public class FileProcessingService {
    private final Queue<String> hashQueue = new ConcurrentLinkedDeque<>();

    public void processFile(String filePath) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                hashQueue.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}
