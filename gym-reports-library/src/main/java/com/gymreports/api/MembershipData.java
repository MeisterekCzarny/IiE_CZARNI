package com.gymreports.api;

public interface MembershipData extends ReportData {
    String getClientName();
    double getAmount();
    String getDate();
}