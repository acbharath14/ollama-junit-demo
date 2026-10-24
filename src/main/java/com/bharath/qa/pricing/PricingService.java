package com.bharath.qa.pricing;

import java.util.List;
import java.util.Objects;

/**
 * Pure pricing logic for a cart: subtotal, tier discount, and total with tax.
 *
 * <p>Discount rules, applied to the subtotal:
 * <ul>
 *   <li>GOLD: 15% when the subtotal is 200 or more, otherwise 10%</li>
 *   <li>SILVER: 5%</li>
 *   <li>BRONZE: no discount</li>
 * </ul>
 *
 * <p>Total = (subtotal - discount) * (1 + taxRate).
 * All methods are pure functions of their arguments; nothing here touches
 * files, networks, or databases, which is what makes the class easy to unit test.
 */
public class PricingService {

    /** Sum of price * quantity over all lines. An empty cart totals 0. */
    public double subtotal(List<LineItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        double sum = 0.0;
        for (LineItem item : items) {
            validate(item);
            sum += item.price() * item.quantity();
        }
        return sum;
    }

    /** Discount rate as a fraction (0.15 means 15%) for a tier at a given subtotal. */
    public double discountRate(CustomerTier tier, double subtotal) {
        Objects.requireNonNull(tier, "tier must not be null");
        if (subtotal < 0) {
            throw new IllegalArgumentException("subtotal must not be negative");
        }
        return switch (tier) {
            case GOLD -> subtotal >= 200.0 ? 0.15 : 0.10;
            case SILVER -> 0.05;
            case BRONZE -> 0.0;
        };
    }

    /** Discount amount in money for a cart and a tier. */
    public double discountAmount(List<LineItem> items, CustomerTier tier) {
        double sub = subtotal(items);
        return sub * discountRate(tier, sub);
    }

    /** Final amount: subtotal minus discount, then tax added. taxRate is a fraction (0.10 = 10%). */
    public double total(List<LineItem> items, CustomerTier tier, double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("taxRate must not be negative");
        }
        double sub = subtotal(items);
        double discounted = sub - discountAmount(items, tier);
        return discounted * (1.0 + taxRate);
    }

    private void validate(LineItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (item.price() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
