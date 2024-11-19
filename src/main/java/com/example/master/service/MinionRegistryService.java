package com.example.master.service;

import com.example.master.model.Minion;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MinionRegistryService {
    private final Map<UUID, Minion> minionRegistry = new ConcurrentHashMap<>();

    public void addMinion(Minion minion) {
        minionRegistry.put(minion.getId(), minion);
    }

    public void removeMinion(UUID minionId) {
        minionRegistry.remove(minionId);
    }

    public List<Minion> getAvailableMinions() {
        return new ArrayList<>(minionRegistry.values());
    }
}
