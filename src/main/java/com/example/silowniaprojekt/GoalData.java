package com.example.silowniaprojekt;

import javafx.beans.property.*;

public class GoalData {
    private final StringProperty exercise;
    private final StringProperty target;

    public GoalData(String exercise, String target) {
        this.exercise = new SimpleStringProperty(exercise);
        this.target = new SimpleStringProperty(target);
    }

    // Gettery i property
    public StringProperty exerciseProperty() { return exercise; }
    public StringProperty targetProperty() { return target; }
}