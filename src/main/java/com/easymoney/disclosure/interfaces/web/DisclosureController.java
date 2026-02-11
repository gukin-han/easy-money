package com.easymoney.disclosure.interfaces.web;

import com.easymoney.disclosure.application.dto.DisclosureInfo;
import com.easymoney.disclosure.application.service.DisclosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/disclosures")
@RequiredArgsConstructor
public class DisclosureController {

    private final DisclosureService disclosureService;

    @GetMapping
    public ResponseEntity<List<DisclosureInfo>> findAll() {
        return ResponseEntity.ok(disclosureService.findAll());
    }
}
