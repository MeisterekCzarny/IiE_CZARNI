package com.gymreports.api;

public interface ProductData extends ReportData {
    String getName();
    double getPrice();
    int getStock();
}