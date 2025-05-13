package com.example.silowniaprojekt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Adres do bazy danych: localhost, port 3306, baza gym_system
    private static final String URL = "jdbc:mysql://localhost:3306/gym_system?serverTimezone=UTC";
    private static final String USER = "root";             // Zmień na odpowiedniego użytkownika
    private static final String PASSWORD = "";    // Zmień na rzeczywiste hasło

    /**
     * Metoda do pobrania połączenia z bazą danych
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Połączenie z bazą danych zostało nawiązane.");
        } catch (SQLException e) {
            System.err.println("Błąd podczas łączenia z bazą danych: " + e.getMessage());
        }
        return connection;
    }

    // Metoda testowa, np. wywołanie main
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            // Połączenie zostało nawiązane – tutaj można wykonać zapytania
            try {
                conn.close();
                System.out.println("Połączenie zostało zamknięte.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
