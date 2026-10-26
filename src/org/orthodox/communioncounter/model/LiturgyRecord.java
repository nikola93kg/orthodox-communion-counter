package org.orthodox.communioncounter.model;

/**
 * Model class representing a single Liturgical Communion record in the Church Chronicle.
 */
public class LiturgyRecord {
    private long id;
    private long timestamp;
    private String dateFormatted;
    private String dayName;
    private String feastName;
    private int count;
    private String notes;

    public LiturgyRecord() {
    }

    public LiturgyRecord(long timestamp, String dateFormatted, String dayName, String feastName, int count, String notes) {
        this.timestamp = timestamp;
        this.dateFormatted = dateFormatted;
        this.dayName = dayName;
        this.feastName = feastName;
        this.count = count;
        this.notes = notes;
    }

    public LiturgyRecord(long id, long timestamp, String dateFormatted, String dayName, String feastName, int count, String notes) {
        this.id = id;
        this.timestamp = timestamp;
        this.dateFormatted = dateFormatted;
        this.dayName = dayName;
        this.feastName = feastName;
        this.count = count;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    public void setDateFormatted(String dateFormatted) {
        this.dateFormatted = dateFormatted;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getFeastName() {
        return feastName;
    }

    public void setFeastName(String feastName) {
        this.feastName = feastName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
