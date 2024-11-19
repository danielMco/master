package com.example.master.service;

import com.example.master.model.Minion;
import com.example.master.model.Result;
import com.example.master.model.Task;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

import static com.example.master.util.ConcurrentFileWriter.writeToFile;


@Service
public class TaskManagerService {
    private final MinionRegistryService minionRegistryService;
    private final RestTemplate restTemplate;

    @Value("${heartbeat.interval.ms:5000}")
    private long heartbeatInterval;

    @Getter
    private final Queue<Task> taskQueue = new ConcurrentLinkedDeque<>();
    @Getter
    private final Map<String, String> crackedPasswords = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerService.class);


    public TaskManagerService(MinionRegistryService minionRegistryService, RestTemplate restTemplate) {
        this.minionRegistryService = minionRegistryService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void startHeartbeatCheck() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkMinionHealth, 0, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    public void registerMinion(Minion minion) {
        minionRegistryService.addMinion(minion);
    }

    public void addTask(Task task) {
        if(!crackedPasswords.containsKey(task.getHash())) {
            taskQueue.add(task);
        }
    }

    public void dispatchTasks() {
        List<Minion> availableMinions = minionRegistryService.getAvailableMinions();

        // If there are no available minions, we simply return
        if (availableMinions.isEmpty()) {
            throw new RuntimeException("No available minions to process tasks.");
        }

        while(!taskQueue.isEmpty()) {
            for (Minion minion: availableMinions) {
                if (taskQueue.isEmpty()) { // for preventing failure due to minion's loop
                    break;
                }
                Task task = taskQueue.peek();
                if(task != null) {
                    try {
                        sendTaskToMinion(minion,task);
                        taskQueue.poll();
                        logger.info("q size: {}",taskQueue.size());
                    } catch (Exception e) {
                        logger.error("Failed to send task to Minion {}: {}", minion.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private void sendTaskToMinion(Minion minion, Task task) {
        String minionUrl = minion.getUrl() + "/receiveTask";
        ResponseEntity<Void> response = restTemplate.postForEntity(minionUrl, task, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Task successfully sent to Minion: {}", minion.getId());
        } else {
            throw new RuntimeException("Minion responded with error status: " + response.getStatusCode());
        }
    }

    public void receiveResult(Result result) {
        if(result.getIsSuccess()) {
            logger.info("minionId - {},hash - {},pass - {}", result.getMinionId(), result.getTask().getHash(), result.getPassword());
            if (result.getPassword() != null && !result.getPassword().isEmpty()) {
                crackedPasswords.put(result.getTask().getHash(), result.getPassword());
                taskQueue.removeIf(task -> task.getHash().equals(result.getTask().getHash()));// Unnecessary tasks
                logger.info("remove task for hash: {}, q size: {}", result.getTask().getHash(), taskQueue.size());
                writeToFile(result);
                logger.info("hash - {},pass - {}", result.getTask().getHash(), result.getPassword());
            }
        } else { // retry in case of minion failure
            logger.info("retry task - {}, minionFail - {}",result.getTask().getTaskId(),result.getMinionId());
            taskQueue.add(result.getTask());
        }
    }

    void checkMinionHealth() {
        List<Minion> minions = minionRegistryService.getAvailableMinions();
        for (Minion m : minions) {
            String heartbeatUrl = m.getUrl() + "/heartbeat";
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(heartbeatUrl, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    minionRegistryService.removeMinion(m.getId());
                }
            } catch (Exception e) {
                minionRegistryService.removeMinion(m.getId());
            }
        }
    }
}
