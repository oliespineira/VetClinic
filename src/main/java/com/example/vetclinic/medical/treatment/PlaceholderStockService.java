package com.example.vetclinic.medical.treatment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TEMPORARY - lets the application start until Person 1's inventory is merged.
 * Logs instead of touching stock, and treats every product as non-prescription.
 * Unit tests mock StockService, so they do not use this class.
 */
@Component
public class PlaceholderStockService implements StockService {

    private static final Logger log = LoggerFactory.getLogger(PlaceholderStockService.class);

    @Override
    public void consume(Long productId, Long locationId, int quantity) {
        log.info("[placeholder] would take {} unit(s) of product {} from location {}",
                quantity, productId, locationId);
    }

    @Override
    public boolean requiresPrescription(Long productId) {
        return false;
    }
}
