package com.example.silowniaprojekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ScheduleData {
    private final StringProperty time;
    private final StringProperty monday;
    private final StringProperty tuesday;
    private final StringProperty wednesday;
    private final StringProperty thursday;
    private final StringProperty friday;
    private final StringProperty saturday;

    public ScheduleData(String time, String monday, String tuesday, String wednesday,
                        String thursday, String friday, String saturday) {
        this.time = new SimpleStringProperty(time);
        this.monday = new SimpleStringProperty(monday);
        this.tuesday = new SimpleStringProperty(tuesday);
        this.wednesday = new SimpleStringProperty(wednesday);
        this.thursday = new SimpleStringProperty(thursday);
        this.friday = new SimpleStringProperty(friday);
        this.saturday = new SimpleStringProperty(saturday);
    }

    // Gettery i property
    public StringProperty timeProperty() { return time; }
    public StringProperty mondayProperty() { return monday; }
    public StringProperty tuesdayProperty() { return tuesday; }
    public StringProperty wednesdayProperty() { return wednesday; }
    public StringProperty thursdayProperty() { return thursday; }
    public StringProperty fridayProperty() { return friday; }
    public StringProperty saturdayProperty() { return saturday; }
}