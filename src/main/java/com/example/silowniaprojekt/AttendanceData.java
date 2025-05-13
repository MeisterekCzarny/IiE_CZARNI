package com.example.silowniaprojekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttendanceData {
    private final StringProperty date;
    private final StringProperty training;
    private final StringProperty client;
    private final StringProperty status;

    public AttendanceData(String date, String training, String client, String status) {
        this.date = new SimpleStringProperty(date);
        this.training = new SimpleStringProperty(training);
        this.client = new SimpleStringProperty(client);
        this.status = new SimpleStringProperty(status);
    }

    // Property getters
    public StringProperty dateProperty() { return date; }
    public StringProperty trainingProperty() { return training; }
    public StringProperty clientProperty() { return client; }
    public StringProperty statusProperty() { return status; }
}