package com.gymreports.api;

public interface TransactionData extends ReportData {
    String getClientName();
    String getProductName();
    String getDate();
    double getAmount();
}