package com.gymreports.api;

import java.io.File;
import java.util.List;

public interface ReportGenerator {

    void generateFinancialReport(ReportConfig config, File outputFile) throws ReportGenerationException;

    void generateProductsReport(ReportConfig config, File outputFile) throws ReportGenerationException;

    void generateMembershipReport(ReportConfig config, File outputFile) throws ReportGenerationException;

    void generateTransactionsReport(ReportConfig config, File outputFile) throws ReportGenerationException;

    List<String> getSupportedFormats();
}