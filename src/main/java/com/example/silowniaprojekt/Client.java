package com.example.silowniaprojekt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();

    public Client(String fullName, String email, String password, String role) {
        this.fullName.set(fullName);
        this.email.set(email);
        this.password.set(password);
        this.role.set(role);
    }

    // Gettery i properties
    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getPassword() { return password.get(); }
    public StringProperty passwordProperty() { return password; }

    public String getRole() { return role.get(); }
    public StringProperty roleProperty() { return role; }
}