package com.minicoinbase;

import java.util.Map;

public class UserEvent {

    private long userId;
    private String eventType;
    private Map<String, Object> eventData;

    public UserEvent(long userId, String eventType, Map<String, Object> eventData) {
        this.userId = userId;
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public UserEvent() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, Object> eventData) {
        this.eventData = eventData;
    }
    // getters and setters

}
