package com.gymreports.impl;

import com.gymreports.api.*;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;  // ✅ KONKRETNY IMPORT
import java.util.List;       // ✅ KONKRETNY IMPORT
import java.util.HashMap;    // ✅ KONKRETNY IMPORT
import java.util.Map;        // ✅ KONKRETNY IMPORT
import java.util.Arrays;     // ✅ KONKRETNY IMPORT

/**
 * Generator raportów dla systemu zarządzania siłownią.
 */
public class ReportGenerator implements com.gymreports.api.ReportGenerator {

    /** Kolor nagłówków w raportach - czerwony (Crimson) */
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(220, 20, 60);

    /** Kolor tła komórek tabeli - ciemny */
    private static final DeviceRgb BACKGROUND_COLOR = new DeviceRgb(26, 26, 26);

    @Override
    public void generateFinancialReport(ReportConfig config, File outputFile) throws ReportGenerationException {
        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            try {
                // Nagłówek raportu z emailem
                addReportHeader(document, "Raport Finansowy", config.getPeriod(), config.getUserEmail());

                // Pobieranie danych z config zamiast z bazy
                List<TransactionData> transactions = config.getTransactions() != null ? config.getTransactions() : new ArrayList<>();
                List<MembershipData> memberships = config.getMemberships() != null ? config.getMemberships() : new ArrayList<>();

                // Konwersja na wewnętrzne typy danych
                List<TransactionDataInternal> internalTransactions = convertTransactions(transactions);
                List<MembershipDataInternal> internalMemberships = convertMemberships(memberships);

                // Sekcja transakcji (sprzedaż produktów)
                addTransactionsSection(document, internalTransactions);

                // Sekcja karnetów (płatności za członkostwo)
                addMembershipsSection(document, internalMemberships);

                // Podsumowanie finansowe
                addFinancialSummary(document, internalTransactions, internalMemberships);

                // Stopka
                addFooter(document);

            } finally {
                document.close();
            }
        } catch (Exception e) {
            throw new ReportGenerationException("Błąd podczas generowania raportu finansowego", e);
        }
    }

    @Override
    public void generateProductsReport(ReportConfig config, File outputFile) throws ReportGenerationException {
        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            try {
                // Nagłówek raportu z emailem
                String selectedProduct = config.getSelectedProduct();
                String title = selectedProduct != null && !selectedProduct.equals("Wszystkie") ?
                        "Raport Produktów - " + selectedProduct : "Raport Produktów";
                addReportHeader(document, title, config.getPeriod(), config.getUserEmail());

                // Pobieranie danych z config
                List<ProductData> products = config.getProducts() != null ? config.getProducts() : new ArrayList<>();
                List<TransactionData> transactions = config.getTransactions() != null ? config.getTransactions() : new ArrayList<>();

                // Konwersja na wewnętrzne typy
                List<ProductDataInternal> internalProducts = convertProducts(products);
                List<TransactionDataInternal> internalTransactions = convertTransactions(transactions);

                // Filtrowanie produktów jeśli wybrano konkretny
                if (selectedProduct != null && !selectedProduct.equals("Wszystkie")) {
                    internalProducts = internalProducts.stream()
                            .filter(p -> p.name.equals(selectedProduct))
                            .collect(java.util.stream.Collectors.toList());

                    // DODANE - filtrowanie transakcji też według produktu
                    internalTransactions = internalTransactions.stream()
                            .filter(t -> t.productName.equals(selectedProduct))
                            .collect(java.util.stream.Collectors.toList());
                }

                // Sekcja aktualnych stanów magazynowych
                addProductsInventorySection(document, internalProducts);

                // Sekcja sprzedaży produktów
                addProductsSalesSection(document, internalTransactions);

                // Podsumowanie
                addProductsSummary(document, internalProducts, internalTransactions);

                // Stopka
                addFooter(document);

            } finally {
                document.close();
            }
        } catch (Exception e) {
            throw new ReportGenerationException("Błąd podczas generowania raportu produktów", e);
        }
    }

    @Override
    public void generateMembershipReport(ReportConfig config, File outputFile) throws ReportGenerationException {
        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            try {
                // Nagłówek raportu z emailem
                addReportHeader(document, "Raport Karnetów", config.getPeriod(), config.getUserEmail());

                // Pobieranie danych z config
                List<MembershipData> memberships = config.getMemberships() != null ? config.getMemberships() : new ArrayList<>();
                List<MembershipDataInternal> internalMemberships = convertMemberships(memberships);

                // Sekcja płatności członkowskich
                addDetailedMembershipsSection(document, internalMemberships);

                // Podsumowanie
                addMembershipSummary(document, internalMemberships);

                // Stopka
                addFooter(document);

            } finally {
                document.close();
            }
        } catch (Exception e) {
            throw new ReportGenerationException("Błąd podczas generowania raportu karnetów", e);
        }
    }

    @Override
    public void generateTransactionsReport(ReportConfig config, File outputFile) throws ReportGenerationException {
        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            try {
                // Nagłówek raportu z emailem
                addReportHeader(document, "Raport Transakcji", config.getPeriod(), config.getUserEmail());

                // Pobieranie danych z config
                List<TransactionData> transactions = config.getTransactions() != null ? config.getTransactions() : new ArrayList<>();
                List<TransactionDataInternal> internalTransactions = convertTransactions(transactions);

                // Sekcja szczegółowa wszystkich transakcji
                addDetailedTransactionsSection(document, internalTransactions);

                // Podsumowanie
                addTransactionsSummary(document, internalTransactions);

                // Stopka
                addFooter(document);

            } finally {
                document.close();
            }
        } catch (Exception e) {
            throw new ReportGenerationException("Błąd podczas generowania raportu transakcji", e);
        }
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList("PDF");
    }

    // Metody konwertujące z interfejsów API na wewnętrzne klasy
    private List<TransactionDataInternal> convertTransactions(List<TransactionData> transactions) {
        return transactions.stream()
                .map(t -> new TransactionDataInternal(t.getId(), t.getClientName(),
                        t.getProductName(), t.getDate(), t.getAmount()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<MembershipDataInternal> convertMemberships(List<MembershipData> memberships) {
        return memberships.stream()
                .map(m -> new MembershipDataInternal(m.getId(), m.getClientName(),
                        m.getAmount(), m.getDate()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<ProductDataInternal> convertProducts(List<ProductData> products) {
        return products.stream()
                .map(p -> new ProductDataInternal(p.getId(), p.getName(),
                        p.getPrice(), p.getStock()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Tworzy czcionkę PDF z obsługą polskich znaków.
     */
    private static PdfFont createFont() throws IOException {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1250, PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            } catch (Exception e2) {
                try {
                    return PdfFontFactory.createFont(StandardFonts.HELVETICA, "windows-1250", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
                } catch (Exception e3) {
                    System.err.println("Nie można załadować czcionki z obsługą polskich znaków, używam podstawowej");
                    return PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
            }
        }
    }

    /**
     * Dodaje nagłówek raportu do dokumentu PDF.
     */
    private static void addReportHeader(Document document, String title, String period, String userEmail) throws IOException {
        PdfFont font = createFont();

        // Logo i informacje o siłowni
        Paragraph header = new Paragraph("SIŁOWNIA FITNESS CENTRUM")
                .setFont(font)
                .setFontSize(20)
                .setFontColor(HEADER_COLOR)
                .setBold();
        document.add(header);

        // Adres i dane kontaktowe
        Paragraph address = new Paragraph("ul. Sportowa 123, 35-340 Rzeszów\nTel: 17 123 45 67\nNIP: 123-456-78-90")
                .setFont(font)
                .setFontSize(10)
                .setMarginBottom(20);
        document.add(address);

        // Tytuł raportu
        Paragraph reportTitle = new Paragraph(title)
                .setFont(font)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(reportTitle);

        // Email użytkownika
        if (userEmail != null && !userEmail.isEmpty()) {
            Paragraph emailParagraph = new Paragraph("Wygenerowany dla: " + userEmail)
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(emailParagraph);
        }

        // Okres raportu
        String periodText = "Okres: " + getPeriodDescription(period);
        Paragraph periodParagraph = new Paragraph(periodText)
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(periodParagraph);

        // Data wygenerowania raportu
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        Paragraph dateParagraph = new Paragraph("Data wygenerowania: " + currentDate)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(30);
        document.add(dateParagraph);
    }

    /**
     * Dodaje sekcję transakcji (sprzedaż produktów) do dokumentu.
     */
    private static void addTransactionsSection(Document document, List<TransactionDataInternal> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Sprzedaż Produktów")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela transakcji
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 30, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (TransactionDataInternal transaction : transactions) {
            table.addCell(createCell(String.valueOf(transaction.id), font));
            table.addCell(createCell(transaction.date, font));
            table.addCell(createCell(transaction.clientName, font));
            table.addCell(createCell(transaction.productName, font));
            table.addCell(createCell(String.format("%.2f", transaction.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję płatności za karnety do dokumentu.
     */
    private static void addMembershipsSection(Document document, List<MembershipDataInternal> memberships) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Płatności za Karnety")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela płatności za karnety
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 55, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (MembershipDataInternal membership : memberships) {
            table.addCell(createCell(String.valueOf(membership.id), font));
            table.addCell(createCell(membership.date, font));
            table.addCell(createCell(membership.clientName, font));
            table.addCell(createCell(String.format("%.2f", membership.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje szczegółową sekcję płatności za karnety do dokumentu.
     */
    private static void addDetailedMembershipsSection(Document document, List<MembershipDataInternal> memberships) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Szczegółowe informacje o płatnościach za karnety")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela płatności za karnety
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 55, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (MembershipDataInternal membership : memberships) {
            table.addCell(createCell(String.valueOf(membership.id), font));
            table.addCell(createCell(membership.date, font));
            table.addCell(createCell(membership.clientName, font));
            table.addCell(createCell(String.format("%.2f", membership.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję stanu magazynowego produktów do dokumentu.
     */
    private static void addProductsInventorySection(Document document, List<ProductDataInternal> products) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Stan Magazynowy Produktów")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela produktów
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 60, 15, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Nazwa Produktu", font));
        table.addHeaderCell(createHeaderCell("Ilość", font));
        table.addHeaderCell(createHeaderCell("Cena (PLN)", font));

        // Dodawanie danych
        for (ProductDataInternal product : products) {
            table.addCell(createCell(String.valueOf(product.id), font));
            table.addCell(createCell(product.name, font));
            table.addCell(createCell(String.valueOf(product.stock), font).setTextAlignment(TextAlignment.CENTER));
            table.addCell(createCell(String.format("%.2f", product.price), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje sekcję sprzedaży produktów z agregacją danych.
     */
    private static void addProductsSalesSection(Document document, List<TransactionDataInternal> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Sprzedaż Produktów w Wybranym Okresie")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Agregacja danych - grupowanie po produktach
        Map<String, Integer> productCounts = new HashMap<>();
        Map<String, Double> productTotals = new HashMap<>();

        for (TransactionDataInternal transaction : transactions) {
            productCounts.merge(transaction.productName, 1, Integer::sum);
            productTotals.merge(transaction.productName, transaction.amount, Double::sum);
        }

        // Tabela sprzedaży z ilościami
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Ilość sprzedana", font));
        table.addHeaderCell(createHeaderCell("Łączna kwota (PLN)", font));
        table.addHeaderCell(createHeaderCell("Średnia cena", font));

        // Dodawanie danych zagregowanych
        for (Map.Entry<String, Integer> entry : productCounts.entrySet()) {
            String productName = entry.getKey();
            Integer count = entry.getValue();
            Double total = productTotals.get(productName);
            Double average = total / count;

            table.addCell(createCell(productName, font));
            table.addCell(createCell(count.toString(), font).setTextAlignment(TextAlignment.CENTER));
            table.addCell(createCell(String.format("%.2f", total), font).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(createCell(String.format("%.2f", average), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje szczegółową sekcję wszystkich transakcji do dokumentu.
     */
    private static void addDetailedTransactionsSection(Document document, List<TransactionDataInternal> transactions) throws IOException {
        PdfFont font = createFont();

        // Nagłówek sekcji
        Paragraph sectionTitle = new Paragraph("Szczegółowe informacje o transakcjach")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // Tabela transakcji
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 30, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Nagłówki tabeli
        table.addHeaderCell(createHeaderCell("ID", font));
        table.addHeaderCell(createHeaderCell("Data", font));
        table.addHeaderCell(createHeaderCell("Klient", font));
        table.addHeaderCell(createHeaderCell("Produkt", font));
        table.addHeaderCell(createHeaderCell("Kwota (PLN)", font));

        // Dodawanie danych
        for (TransactionDataInternal transaction : transactions) {
            table.addCell(createCell(String.valueOf(transaction.id), font));
            table.addCell(createCell(transaction.date, font));
            table.addCell(createCell(transaction.clientName, font));
            table.addCell(createCell(transaction.productName, font));
            table.addCell(createCell(String.format("%.2f", transaction.amount), font).setTextAlignment(TextAlignment.RIGHT));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Dodaje podsumowanie finansowe do dokumentu.
     */
    private static void addFinancialSummary(Document document, List<TransactionDataInternal> transactions,
                                            List<MembershipDataInternal> memberships) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double transactionsTotal = transactions.stream().mapToDouble(t -> t.amount).sum();
        double membershipsTotal = memberships.stream().mapToDouble(m -> m.amount).sum();
        double total = transactionsTotal + membershipsTotal;

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - suma ze sprzedaży produktów
        table.addCell(createSummaryLabelCell("Przychody ze sprzedaży produktów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", transactionsTotal), font));

        // Wiersz - suma z płatności za karnety
        table.addCell(createSummaryLabelCell("Przychody z karnetów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", membershipsTotal), font));

        // Wiersz - suma całkowita
        table.addCell(createSummaryLabelCell("SUMA PRZYCHODÓW:", font).setBold());
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", total), font).setBold());

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie produktów do dokumentu.
     */
    private static void addProductsSummary(Document document, List<ProductDataInternal> products, List<TransactionDataInternal> transactions) throws IOException {
        PdfFont font = createFont();

        // Obliczanie wartości magazynu
        double inventoryValue = products.stream().mapToDouble(p -> p.price * p.stock).sum();

        // Obliczanie sprzedaży całkowitej
        double salesTotal = transactions.stream().mapToDouble(t -> t.amount).sum();

        // Obliczanie ilości sprzedanych produktów
        int totalProductsSold = transactions.size();

        // Obliczanie ilości różnych produktów sprzedanych
        long uniqueProductsSold = transactions.stream()
                .map(t -> t.productName)
                .distinct()
                .count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - wartość magazynu
        table.addCell(createSummaryLabelCell("Wartość stanu magazynowego:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", inventoryValue), font));

        // Wiersz - sprzedaż w okresie
        table.addCell(createSummaryLabelCell("Sprzedaż w wybranym okresie:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", salesTotal), font));

        // Wiersz - liczba sprzedanych produktów
        table.addCell(createSummaryLabelCell("Łączna ilość sprzedanych produktów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(totalProductsSold), font));

        // Wiersz - liczba różnych produktów sprzedanych
        table.addCell(createSummaryLabelCell("Liczba różnych produktów sprzedanych:", font));
        table.addCell(createSummaryValueCell(String.valueOf(uniqueProductsSold), font));

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie karnetów do dokumentu.
     */
    private static void addMembershipSummary(Document document, List<MembershipDataInternal> memberships) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double membershipsTotal = memberships.stream().mapToDouble(m -> m.amount).sum();
        int clientCount = (int) memberships.stream().map(m -> m.clientName).distinct().count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - liczba klientów z karnetami
        table.addCell(createSummaryLabelCell("Liczba klientów z karnetami:", font));
        table.addCell(createSummaryValueCell(String.valueOf(clientCount), font));

        // Wiersz - suma z płatności za karnety
        table.addCell(createSummaryLabelCell("Przychody z karnetów:", font));
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", membershipsTotal), font));

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje podsumowanie transakcji do dokumentu.
     */
    private static void addTransactionsSummary(Document document, List<TransactionDataInternal> transactions) throws IOException {
        PdfFont font = createFont();

        // Obliczanie sum
        double transactionsTotal = transactions.stream().mapToDouble(t -> t.amount).sum();
        int clientCount = (int) transactions.stream().map(t -> t.clientName).distinct().count();
        int productCount = (int) transactions.stream().map(t -> t.productName).distinct().count();

        // Tabela podsumowania
        Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(null);

        // Wiersz - liczba transakcji
        table.addCell(createSummaryLabelCell("Liczba transakcji:", font));
        table.addCell(createSummaryValueCell(String.valueOf(transactions.size()), font));

        // Wiersz - liczba klientów
        table.addCell(createSummaryLabelCell("Liczba klientów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(clientCount), font));

        // Wiersz - liczba sprzedanych produktów
        table.addCell(createSummaryLabelCell("Liczba różnych produktów:", font));
        table.addCell(createSummaryValueCell(String.valueOf(productCount), font));

        // Wiersz - suma transakcji
        table.addCell(createSummaryLabelCell("Suma transakcji:", font).setBold());
        table.addCell(createSummaryValueCell(String.format("%.2f PLN", transactionsTotal), font).setBold());

        Div summaryDiv = new Div()
                .setMarginTop(30)
                .add(new Paragraph("PODSUMOWANIE").setFont(font).setFontSize(14).setBold().setMarginBottom(10))
                .add(table);

        document.add(summaryDiv);
    }

    /**
     * Dodaje stopkę do dokumentu PDF.
     */
    private static void addFooter(Document document) throws IOException {
        PdfFont font = createFont();

        Paragraph footer = new Paragraph("Siłownia Fitness Centrum - System Zarządzania")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        document.add(footer);
    }

    /**
     * Tworzy komórkę nagłówka tabeli o określonym stylu.
     */
    private static Cell createHeaderCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }

    /**
     * Tworzy standardową komórkę tabeli o określonym stylu.
     */
    private static Cell createCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBackgroundColor(BACKGROUND_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(5);
    }

    /**
     * Tworzy komórkę etykiety w tabeli podsumowania.
     */
    private static Cell createSummaryLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    /**
     * Tworzy komórkę wartości w tabeli podsumowania.
     */
    private static Cell createSummaryValueCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
    }

    /**
     * Konwertuje string okresu na czytelny opis dla użytkownika.
     */
    private static String getPeriodDescription(String period) {
        // Sprawdź czy period zawiera konkretne daty (format: "YYYY-MM-DD:YYYY-MM-DD")
        if (period.contains(":")) {
            String[] dates = period.split(":");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            try {
                LocalDate startDate = LocalDate.parse(dates[0]);
                LocalDate endDate = LocalDate.parse(dates[1]);
                return "Od " + startDate.format(formatter) + " do " + endDate.format(formatter);
            } catch (Exception e) {
                return period;
            }
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "Ostatni tydzień":
                startDate = endDate.minusWeeks(1);
                break;
            case "Ostatni miesiąc":
                startDate = endDate.minusMonths(1);
                break;
            case "Ostatni kwartał":
                startDate = endDate.minusMonths(3);
                break;
            case "Bieżący rok":
                startDate = LocalDate.of(endDate.getYear(), 1, 1);
                break;
            case "Wszystkie":
                startDate = LocalDate.of(2000, 1, 1);
                break;
            case "Bieżący stan":
                startDate = endDate;
                break;
            default:
                startDate = endDate.minusMonths(1);
        }

        LocalDate[] dateRange = new LocalDate[] { startDate, endDate };
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (period.equals("Bieżący stan")) {
            return "Stan na dzień " + endDate.format(formatter);
        } else {
            return "Od " + startDate.format(formatter) + " do " + endDate.format(formatter);
        }
    }

    // Wewnętrzne klasy danych
    public static class TransactionDataInternal {
        public final int id;
        public final String clientName;
        public final String productName;
        public final String date;
        public final double amount;

        public TransactionDataInternal(int id, String clientName, String productName, String date, double amount) {
            this.id = id;
            this.clientName = clientName;
            this.productName = productName;
            this.date = date;
            this.amount = amount;
        }
    }

    public static class MembershipDataInternal {
        public final int id;
        public final String clientName;
        public final double amount;
        public final String date;

        public MembershipDataInternal(int id, String clientName, double amount, String date) {
            this.id = id;
            this.clientName = clientName;
            this.amount = amount;
            this.date = date;
        }
    }

    public static class ProductDataInternal {
        public final int id;
        public final String name;
        public final double price;
        public final int stock;

        public ProductDataInternal(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }
    }
}