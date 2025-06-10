package com.gymreports.api;

import java.time.LocalDate;
import java.util.List;

public class ReportConfig {
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String userEmail;
    private String selectedProduct;
    private List<TransactionData> transactions;
    private List<MembershipData> memberships;
    private List<ProductData> products;

    // Informacje o firmie
    private String companyName = "SIŁOWNIA FITNESS CENTRUM";
    private String companyAddress = "ul. Sportowa 123, 35-340 Rzeszów";
    private String companyPhone = "17 123 45 67";
    private String companyNip = "123-456-78-90";

    // Konstruktor
    public ReportConfig() {}

    public ReportConfig(String period, String userEmail) {
        this.period = period;
        this.userEmail = userEmail;
    }

    // Metody fabryczne
    public static ReportConfig forPeriod(String period, String userEmail) {
        return new ReportConfig(period, userEmail);
    }

    // Builder methods
    public ReportConfig withTransactions(List<TransactionData> transactions) {
        this.transactions = transactions;
        return this;
    }

    public ReportConfig withMemberships(List<MembershipData> memberships) {
        this.memberships = memberships;
        return this;
    }

    public ReportConfig withProducts(List<ProductData> products) {
        this.products = products;
        return this;
    }

    public ReportConfig withSelectedProduct(String selectedProduct) {
        this.selectedProduct = selectedProduct;
        return this;
    }

    // Gettery
    public String getPeriod() { return period; }
    public String getUserEmail() { return userEmail; }
    public String getSelectedProduct() { return selectedProduct; }
    public List<TransactionData> getTransactions() { return transactions; }
    public List<MembershipData> getMemberships() { return memberships; }
    public List<ProductData> getProducts() { return products; }
    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getCompanyPhone() { return companyPhone; }
    public String getCompanyNip() { return companyNip; }

    // Settery
    public void setPeriod(String period) { this.period = period; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setSelectedProduct(String selectedProduct) { this.selectedProduct = selectedProduct; }
    public void setTransactions(List<TransactionData> transactions) { this.transactions = transactions; }
    public void setMemberships(List<MembershipData> memberships) { this.memberships = memberships; }
    public void setProducts(List<ProductData> products) { this.products = products; }
}