package com.example.es.payment.controller;

import com.example.es.payment.service.VivaWalletService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class RestPaymentController {

    private final VivaWalletService vivaWalletService;

    public RestPaymentController(VivaWalletService vivaWalletService) {
        this.vivaWalletService = vivaWalletService;
    }

    @GetMapping("/calculate-vat")
    public Map<String, Object> calculateVAT(@RequestParam BigDecimal amount) {
        BigDecimal vat = vivaWalletService.calculateVatAmount(amount);
        BigDecimal total = vivaWalletService.calculateTotalWithVat(amount);
        return Map.of(
            "baseAmount", amount,
            "vatRate", "24%",
            "vatAmount", vat,
            "totalWithVat", total
        );
    }
}
