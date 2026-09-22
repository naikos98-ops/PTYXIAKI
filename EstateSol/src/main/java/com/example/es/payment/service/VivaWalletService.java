package com.example.es.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VivaWalletService {

    private static final Logger log = LoggerFactory.getLogger(VivaWalletService.class);
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.24);
    public BigDecimal calculateVatAmount(BigDecimal baseAmount) {
        if (baseAmount == null) return BigDecimal.ZERO;
        return baseAmount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateTotalWithVat(BigDecimal baseAmount) {
        if (baseAmount == null) return BigDecimal.ZERO;
        BigDecimal vat = calculateVatAmount(baseAmount);
        return baseAmount.add(vat).setScale(2, RoundingMode.HALF_UP);
    }
    public boolean holdFunds(String transactionId, BigDecimal amountBeforeVat) {
        BigDecimal totalAmount = calculateTotalWithVat(amountBeforeVat);
        log.info(">>> Viva.com Smart Checkout: Freezing escrow funds for transaction {}.", transactionId);
        log.info("Base Amount: {} €, VAT (24%): {} €, Total Frozen: {} €", amountBeforeVat, calculateVatAmount(amountBeforeVat), totalAmount);
        return true;
    }
    public boolean releaseMilestoneFunds(String transactionId, BigDecimal amountToReleaseBeforeVat) {
        BigDecimal totalRelease = calculateTotalWithVat(amountToReleaseBeforeVat);
        log.info(">>> Viva.com Smart Checkout: Releasing escrow funds for transaction {}.", transactionId);
        log.info("Released Base Amount: {} €, Released VAT (24%): {} €, Total Released: {} €", amountToReleaseBeforeVat, calculateVatAmount(amountToReleaseBeforeVat), totalRelease);
        return true;
    }
}
