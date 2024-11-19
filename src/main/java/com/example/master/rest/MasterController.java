package com.example.master.rest;

import com.example.master.model.Minion;
import com.example.master.model.Result;
import com.example.master.service.TaskManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/master")
public class MasterController {

    @Autowired
    private TaskManagerService taskManagerService;

    @PostMapping("/registerMinion")
    public ResponseEntity<String> registerMinion(@RequestBody Minion minion) {
        try {
            taskManagerService.registerMinion(minion);
            return ResponseEntity.ok("Minion registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering minion: " + e.getMessage());
        }
    }

    @PostMapping("/receiveResult")
    public ResponseEntity<String> receiveResult(@RequestBody Result result) {
        try {
            taskManagerService.receiveResult(result);
            return ResponseEntity.ok("Result received successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error receiving result: " + e.getMessage());
        }
    }

}
