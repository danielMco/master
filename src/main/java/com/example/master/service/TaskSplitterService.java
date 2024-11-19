package com.example.master.service;

import com.example.master.model.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.UUID;

@Service
public class TaskSplitterService {

    @Value("${task.split.size:100}")
    private int numberOfSubranges;

    private final TaskManagerService taskManager;

    public TaskSplitterService(TaskManagerService taskManager) {
        this.taskManager = taskManager;
    }

    public void splitTasks(Queue<String> hashQueue) {
        hashQueue.parallelStream().forEach(this::createTasksForHash);
    }

    private void createTasksForHash(String hash) {
        long startRange = 500000000L;
        long endRange = 599999999L;

        long rangeSize = (endRange - startRange +1) / numberOfSubranges;

        for(int i=0; i<numberOfSubranges; i++) {
            long subrangeStart = startRange + (i * rangeSize);
            long subrangeEnd = (i == numberOfSubranges - 1) ? endRange : subrangeStart + rangeSize - 1;
            Task task = new Task(UUID.randomUUID(),hash,subrangeStart,subrangeEnd,false);
            taskManager.addTask(task);
        }
    }
}
