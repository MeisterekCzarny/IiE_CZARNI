package com.example.silowniaprojekt;

import javafx.beans.property.*;

public class ProgressData {
    private final StringProperty exerciseName;
    private final DoubleProperty progress;
    private final StringProperty target;

    public ProgressData(String exerciseName, double progress, String target) {
        this.exerciseName = new SimpleStringProperty(exerciseName);
        this.progress = new SimpleDoubleProperty(progress);
        this.target = new SimpleStringProperty(target);
    }

    // Gettery i property
    public StringProperty exerciseNameProperty() { return exerciseName; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty targetProperty() { return target; }
}