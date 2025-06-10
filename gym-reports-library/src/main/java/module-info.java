module gym.reports.library {
    // Wymagane moduły
    requires kernel;
    requires layout;
    requires io;
    requires java.desktop;

    // Eksportuj pakiety API
    exports com.gymreports.api;
    exports com.gymreports.impl;
}