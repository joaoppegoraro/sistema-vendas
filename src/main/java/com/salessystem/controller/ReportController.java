package com.salessystem.controller;

import com.salessystem.service.CsvExportService;
import com.salessystem.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final CsvExportService csvExportService;

    public ReportController(ReportService reportService, CsvExportService csvExportService) {
        this.reportService = reportService;
        this.csvExportService = csvExportService;
    }

    @GetMapping
    public String index() {
        return "reports/index";
    }

    @GetMapping("/annual")
    public String annual(@RequestParam(required = false) Integer year, Model model) {
        int resolvedYear = year != null ? year : Year.now().getValue();
        model.addAttribute("report", reportService.getAnnualRevenue(resolvedYear));
        model.addAttribute("year", resolvedYear);
        return "reports/annual";
    }

    @GetMapping("/invoices")
    public String invoices(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                            Model model) {
        model.addAttribute("sales", reportService.getInvoiceMirror(date, month));
        model.addAttribute("date", date);
        model.addAttribute("month", month != null ? month : (date == null ? YearMonth.now() : null));
        return "reports/invoices";
    }

    @GetMapping("/daily-cash")
    public String dailyCash(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        LocalDate resolvedDate = date != null ? date : LocalDate.now();
        model.addAttribute("report", reportService.getDailyCashClosing(resolvedDate));
        return "reports/daily-cash";
    }

    @GetMapping("/clients")
    public String clientAnalytics(@RequestParam(defaultValue = "60") int inactiveDays, Model model) {
        model.addAttribute("report", reportService.getClientAnalytics(inactiveDays));
        return "reports/clients";
    }

    @GetMapping("/profitability")
    public String profitability(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                                 Model model) {
        YearMonth resolvedMonth = month != null ? month : YearMonth.now();
        model.addAttribute("report", reportService.getProfitabilityReport(resolvedMonth));
        model.addAttribute("month", resolvedMonth);
        return "reports/profitability";
    }

    @GetMapping("/sales/export")
    public void exportSales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                             HttpServletResponse response) throws IOException {
        csvExportService.exportSales(response, date, month);
    }

    @GetMapping("/stock/export")
    public void exportStock(HttpServletResponse response) throws IOException {
        csvExportService.exportStock(response);
    }
}
