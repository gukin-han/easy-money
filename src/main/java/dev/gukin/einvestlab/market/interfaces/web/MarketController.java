package dev.gukin.einvestlab.market.interfaces.web;

import dev.gukin.einvestlab.market.application.dto.MarketReactionInfo;
import dev.gukin.einvestlab.market.application.service.MarketReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market-reactions")
@RequiredArgsConstructor
public class MarketController {

    private final MarketReactionService marketReactionService;

    @GetMapping
    public ResponseEntity<List<MarketReactionInfo>> findAll() {
        return ResponseEntity.ok(marketReactionService.findAll());
    }
}
