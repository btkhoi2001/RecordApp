package com.ag18.record;

public class RecordingItem {
    private String title;
    private String createdAt;
    private String duration;

    RecordingItem(String title) {
        this.title = title;
        /*this.createdAt = createdAt;
        this.duration = duration;*/
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDuration() {
        return duration;
    }

}
