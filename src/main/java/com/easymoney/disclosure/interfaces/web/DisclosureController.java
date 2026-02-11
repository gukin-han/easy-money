package com.easymoney.disclosure.interfaces.web;

import com.easymoney.disclosure.application.dto.DisclosureInfo;
import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import com.easymoney.disclosure.application.service.DisclosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disclosures")
@RequiredArgsConstructor
public class DisclosureController {

    private final DisclosureService disclosureService;
    private final DisclosureCollectionService disclosureCollectionService;

    @GetMapping
    public ResponseEntity<List<DisclosureInfo>> findAll() {
        return ResponseEntity.ok(disclosureService.findAll());
    }

    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collect() {
        int count = disclosureCollectionService.collect();
        return ResponseEntity.ok(Map.of("collected", count));
    }
}
