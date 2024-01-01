package calendar.data.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Event {
    private int eventId;
    private String eventName;
    private int hostId;
    private String description;
    private long startAt;
    private long endAt;
    private boolean allDay;
    private int interval;
    private int timeframe;

    private int timeLeft; // 단위: minute

    private boolean firstDay;
    private boolean lastDay;

    private int startDay;
    private int endDay;

    public Event() {

    }
    public Event(String eventName, String description, long startAt, long endAt, boolean allDay, int interval, int timeframe) {
        this(-1, eventName, -1, description, startAt, endAt, allDay, interval, timeframe);
    }
    public Event(int eventId, String eventName, int hostId, String description, long startAt, long endAt, boolean allDay,
                 int interval, int timeframe) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.hostId = hostId;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDay = allDay;
        this.interval = interval;
        this.timeframe = timeframe;
        this.firstDay = false;
    }

    public int getEventId() {
        return eventId;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getHostId() {
        return hostId;
    }
    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartAt() {
        return startAt;
    }
    public String getStartTime() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startAt), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }
    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getEndAt() {
        return endAt;
    }
    public void setEndAt(long endAt) {
        this.endAt = endAt;
    }

    public boolean isAllDay() {
        return allDay;
    }
    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getTimeframe() {
        return timeframe;
    }
    public void setTimeframe(int timeframe) {
        this.timeframe = timeframe;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public boolean isFirstDay() {
        return firstDay;
    }
    public void setFirstDay(boolean firstDay) {
        this.firstDay = firstDay;
    }
    public boolean isLastDay() {
        return lastDay;
    }
    public void setLastDay(boolean lastDay) {
        this.lastDay = lastDay;
    }

    public int getStartDay() {
        return startDay;
    }
    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }
    public int getEndDay() {
        return endDay;
    }
    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }
}
