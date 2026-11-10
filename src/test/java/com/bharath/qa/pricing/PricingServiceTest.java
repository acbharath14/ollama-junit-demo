package com.bharath.qa.pricing;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Hand-written tests for {@link PricingService}. These are the suite's baseline:
 * they prove the app works and show the naming and assertion style that
 * Ollama-generated tests should follow. Test names state the behavior and
 * the expected outcome, in plain words.
 *
 * Boundary variants of one rule share a parameterized test instead of living
 * in copied test methods. The null-list NPE test stays separate: null is a
 * different failure polarity, not a valid-input variant.
 */
class PricingServiceTest {

    private static final double DELTA = 0.001;

    private final PricingService pricing = new PricingService();

    @ParameterizedTest(name = "subtotal({0}) -> {1}")
    @MethodSource("subtotalCases")
    void subtotalMultipliesPriceByQuantityForEachLine(List<LineItem> items, double expected) {
        assertEquals(expected, pricing.subtotal(items), DELTA);
    }

    static Stream<Arguments> subtotalCases() {
        return Stream.of(
                // Multi-line cart: (10 x 2) + (5 x 3) = 35.
                Arguments.of(List.of(
                        new LineItem("prod-1", 10.0, 2),
                        new LineItem("prod-2", 5.0, 3)), 35.0),
                // Empty cart: the same formula over zero lines is 0.
                Arguments.of(List.of(), 0.0));
    }

    @ParameterizedTest(name = "GOLD subtotal {0} is discounted at rate {1}")
    @CsvSource({"200.0, 0.15", "199.99, 0.10"})
    void goldDiscountIsFifteenPercentAtThreshold(double subtotal, double rate) {
        List<LineItem> items = List.of(new LineItem("prod-1", subtotal, 1));
        assertEquals(subtotal * rate, pricing.discountAmount(items, CustomerTier.GOLD), DELTA);
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

    @ParameterizedTest(name = "price={0}, quantity={1} is rejected")
    @CsvSource({"-1.0, 1", "10.0, 0", "10.0, -1"})
    void negativePriceThrowsIllegalArgumentException(double price, int quantity) {
        List<LineItem> items = List.of(new LineItem("prod-1", price, quantity));
        assertThrows(IllegalArgumentException.class, () -> pricing.subtotal(items));
    }

    @Test
    void nullItemListThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> pricing.subtotal(null));
    }
}
