package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplicatedLog {
    private final List<Map<String, String>> entries = new ArrayList<>();

    public void addEntry(String id, String message) {
        Map<String, String> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("message", message);
        entry.put("state", "PENDING");
        entries.add(entry);
    }

    public List<Map<String, String>> getEntries() {
        return entries;
    }

    public Map<String, String> findEntryById(String id) {
        for (Map<String, String> entry : entries) {
            if (entry.get("id").equals(id)) {
                return entry;
            }
        }
        return null;
    }

    public void commitEntry(String id) {
        Map<String, String> entry = findEntryById(id);
        if (entry != null) {
            entry.put("state", "COMMITTED");
        }
    }

    public List<Map<String, String>> getCommittedEntries() {
        return entries.stream()
                .filter(entry -> "COMMITTED".equals(entry.get("state")))
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> getPendingEntries() {
        return entries.stream()
                .filter(entry -> "PENDING".equals(entry.get("state")))
                .collect(Collectors.toList());
    }

    public String getLog(int maxQuantity) {
        int start = 0;
        if (maxQuantity > 0) {
            start = Math.max(0, entries.size() - maxQuantity);
        }
        String strLog = "";
        for (int i = start; i < entries.size(); i++) {
            Map<String, String> entry = entries.get(i);
            strLog = strLog + "ID: " + entry.get("id") + ", State: " + entry.get("state")
                    + ", Message: " + entry.get("message") + "\n";
        }
        return strLog;
    }
}