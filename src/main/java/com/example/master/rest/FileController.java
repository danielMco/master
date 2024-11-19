package com.example.master.rest;

import com.example.master.service.FileProcessingService;
import com.example.master.service.TaskManagerService;
import com.example.master.service.TaskSplitterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileProcessingService fileProcessingService;
    private final TaskSplitterService taskSplitterService;
    private final TaskManagerService taskManagerService;

    public FileController(FileProcessingService fileProcessingService, TaskSplitterService taskSplitterService, TaskManagerService taskManagerService) {
        this.fileProcessingService = fileProcessingService;
        this.taskSplitterService = taskSplitterService;
        this.taskManagerService = taskManagerService;
    }

    @PostMapping("/uploadAndCrack")
    public ResponseEntity<String> uploadFile(@RequestParam("filePath") String filePath) {
        try {
            fileProcessingService.processFile(filePath);
            Queue<String> hashQueue = fileProcessingService.getHashQueue();

            taskSplitterService.splitTasks(hashQueue);
            taskManagerService.dispatchTasks();
            return ResponseEntity.ok("File processed, tasks created, and cracking process started.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during file upload and processing: " + e.getMessage());
        }
    }
}
