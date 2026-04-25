package dev.gukin.einvestlab.analysis.interfaces.web;

import dev.gukin.einvestlab.analysis.application.dto.AnalysisReportInfo;
import dev.gukin.einvestlab.analysis.application.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public ResponseEntity<List<AnalysisReportInfo>> findAll() {
        return ResponseEntity.ok(analysisService.findAll());
    }
}
