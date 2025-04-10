package utils;

import java.util.List;
import java.util.ArrayList;

public class Log {
    private List<String> events;

    public Log() {
        this.events = new ArrayList<>();
    }

    public void add(String event) {
        events.add(event);
    }

    public String getLog(int maxQuantity) {
        int start = Math.max(0, events.size() - maxQuantity);
        String eventsLog = "";
        for (int i = start; i < events.size(); i++) {
            eventsLog += (i + 1) + ": " + events.get(i) + "\n";
        }
        return eventsLog;
    }

}
