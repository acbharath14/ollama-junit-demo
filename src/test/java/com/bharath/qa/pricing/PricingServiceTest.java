package com.bharath.qa.pricing;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Hand-written tests for {@link PricingService}. These are the suite's baseline:
 * they prove the app works and show the naming and assertion style that
 * Ollama-generated tests should follow. Test names state the behavior and
 * the expected outcome, in plain words.
 */
class PricingServiceTest {

    private static final double DELTA = 0.001;

    private final PricingService pricing = new PricingService();

    @Test
    void subtotalMultipliesPriceByQuantityForEachLine() {
        List<LineItem> items = List.of(
                new LineItem("prod-1", 10.0, 2),
                new LineItem("prod-2", 5.0, 3));
        assertEquals(35.0, pricing.subtotal(items), DELTA);
    }

    @Test
    void emptyCartSubtotalIsZero() {
        assertEquals(0.0, pricing.subtotal(List.of()), DELTA);
    }

    @Test
    void goldDiscountIsFifteenPercentAtThreshold() {
        List<LineItem> items = List.of(new LineItem("prod-1", 100.0, 2));
        assertEquals(30.0, pricing.discountAmount(items, CustomerTier.GOLD), DELTA);
    }

    @Test
    void goldDiscountIsTenPercentBelowThreshold() {
        List<LineItem> items = List.of(new LineItem("prod-1", 199.99, 1));
        assertEquals(19.999, pricing.discountAmount(items, CustomerTier.GOLD), DELTA);
    }

    @Test
    void silverDiscountIsFivePercent() {
        List<LineItem> items = List.of(new LineItem("prod-1", 100.0, 1));
        assertEquals(5.0, pricing.discountAmount(items, CustomerTier.SILVER), DELTA);
    }

    @Test
    void bronzeDiscountIsZero() {
        List<LineItem> items = List.of(new LineItem("prod-1", 100.0, 1));
        assertEquals(0.0, pricing.discountAmount(items, CustomerTier.BRONZE), DELTA);
    }

    @Test
    void totalSubtractsDiscountThenAddsTax() {
        List<LineItem> items = List.of(new LineItem("prod-1", 100.0, 2));
        // Subtotal 200, GOLD discount 30, taxed at 10%: (200 - 30) * 1.10 = 187.
        assertEquals(187.0, pricing.total(items, CustomerTier.GOLD, 0.10), DELTA);
    }

    @Test
    void negativePriceThrowsIllegalArgumentException() {
        List<LineItem> items = List.of(new LineItem("prod-1", -1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> pricing.subtotal(items));
    }

    @Test
    void zeroQuantityThrowsIllegalArgumentException() {
        List<LineItem> items = List.of(new LineItem("prod-1", 10.0, 0));
        assertThrows(IllegalArgumentException.class, () -> pricing.subtotal(items));
    }

    @Test
    void nullItemListThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> pricing.subtotal(null));
    }
}
