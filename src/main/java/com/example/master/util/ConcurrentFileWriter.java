package com.example.master.util;

import com.example.master.model.Result;
import com.example.master.service.TaskManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentFileWriter {
    private static final Lock lock = new ReentrantLock();
    private static final String FILE_NAME = "output.txt";

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerService.class);

    public static void writeToFile(Result result) {
        lock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            String line = String.format("Hash: %s -> Password: %s%n", result.getTask().getHash(), result.getPassword());
            writer.write(line);
            writer.newLine();
        } catch (Exception e) {
            logger.error("Error during output cracked password to file: {}",e.getMessage());
        } finally {
            lock.unlock();
        }
    }

}
