package com.fekozma.wallpaperchanger.models;

public class TimeLabel {
    private String name;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private boolean isPermanent; // true for default labels (Morning, Midday, etc.)
    private String id; // unique identifier for storage

    public TimeLabel(String name, int startHour, int startMinute, int endHour, int endMinute, boolean isPermanent, String id) {
        this.name = name;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.isPermanent = isPermanent;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeRangeString() {
        return String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
    }

    // Convert to JSON-like string for storage
    public String toStorageString() {
        return name + "|" + startHour + "|" + startMinute + "|" + endHour + "|" + endMinute + "|" + isPermanent + "|" + id;
    }

    // Parse from storage string
    public static TimeLabel fromStorageString(String str) {
        String[] parts = str.split("\\|");
        if (parts.length == 7) {
            return new TimeLabel(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                Boolean.parseBoolean(parts[5]),
                parts[6]
            );
        }
        return null;
    }
}
