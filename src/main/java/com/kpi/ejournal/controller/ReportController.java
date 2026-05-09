package com.kpi.ejournal.controller;

import com.kpi.ejournal.dto.GenerateReportRequest;
import com.kpi.ejournal.dto.ReportDetailsResponse;
import com.kpi.ejournal.dto.ReportResponse;
import com.kpi.ejournal.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/generate")
    public ReportResponse generate(@Valid @RequestBody GenerateReportRequest request) {
        return reportService.generate(request);
    }

    @GetMapping
    public List<ReportResponse> getAll() {
        return reportService.getAll();
    }

    @GetMapping("/{id}")
    public ReportDetailsResponse getDetails(@PathVariable("id") Long id) {
        return reportService.getDetails(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportService.exportPdf(id));
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(reportService.exportExcel(id));
    }
}
