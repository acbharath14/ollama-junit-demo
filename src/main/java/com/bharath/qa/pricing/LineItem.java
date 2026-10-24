package com.bharath.qa.pricing;

/**
 * One line in a cart: a product, its unit price, and how many units.
 *
 * @param sku      product identifier, for example "prod-1"
 * @param price    unit price, must not be negative
 * @param quantity number of units, must be positive
 */
public record LineItem(String sku, double price, int quantity) {
}
