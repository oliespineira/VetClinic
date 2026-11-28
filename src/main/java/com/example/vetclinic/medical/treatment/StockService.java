package com.example.vetclinic.medical.treatment;

/**
 * What the treatment slice needs from inventory. Defined here, on the side that
 * uses it, so treatments do not depend on how Person 1 builds the inventory.
 * When the real InventoryService exists, one small class implements this
 * interface by calling it, and PlaceholderStockService is deleted.
 */
public interface StockService {

    /** Takes {@code quantity} units of a product from a location's stock. */
    void consume(Long productId, Long locationId, int quantity);

    /** Whether the product can only be given out by a vet. */
    boolean requiresPrescription(Long productId);
}
