package com.velora.service;

import com.velora.vehicle.Vehicle;
import com.velora.vehicle.VehicleType;

import java.time.Clock;
import java.time.LocalDate;

/** Central source of truth for rental promotions, loyalty discounts and tax. */
public final class RentalPricingService {

    public static final double TAX_RATE = 0.10;
    public static final double LOYALTY_DISCOUNT_RATE = 0.10;
    public static final LocalDate SUMMER_PROMOTION_END = LocalDate.of(2026, 8, 31);

    private final Clock clock;

    public RentalPricingService() {
        this(Clock.systemDefaultZone());
    }

    public RentalPricingService(Clock clock) {
        this.clock = clock == null ? Clock.systemDefaultZone() : clock;
    }

    public PricingQuote quote(Vehicle vehicle, int rentalDays, boolean useLoyaltyDiscount) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }
        if (rentalDays < 1 || rentalDays > 30) {
            throw new IllegalArgumentException("Rental days must be between 1 and 30.");
        }

        double originalSubtotal = money(vehicle.getDailyPrice() * rentalDays);
        double promotionRate = promotionRate(vehicle.getType());
        double promotionDiscount = money(originalSubtotal * promotionRate);
        double afterPromotion = money(originalSubtotal - promotionDiscount);
        double loyaltyDiscount = useLoyaltyDiscount
                ? money(afterPromotion * LOYALTY_DISCOUNT_RATE)
                : 0.0;
        double taxableSubtotal = money(afterPromotion - loyaltyDiscount);
        double tax = money(taxableSubtotal * TAX_RATE);

        return new PricingQuote(
                originalSubtotal,
                promotionRate,
                promotionDiscount,
                loyaltyDiscount,
                taxableSubtotal,
                tax,
                money(taxableSubtotal + tax)
        );
    }

    public double promotionRate(VehicleType type) {
        if (LocalDate.now(clock).isAfter(SUMMER_PROMOTION_END) || type == null) {
            return 0.0;
        }
        return switch (type) {
            case MOTORCYCLE, ELECTRIC_BIKE -> 0.30;
            case TRUCK -> 0.20;
            default -> 0.0;
        };
    }

    private static double money(double amount) {
        return Math.round(Math.max(0.0, amount) * 100.0) / 100.0;
    }

    public record PricingQuote(
            double originalSubtotal,
            double promotionRate,
            double promotionDiscount,
            double loyaltyDiscount,
            double taxableSubtotal,
            double tax,
            double total
    ) {
        public double totalDiscount() {
            return promotionDiscount + loyaltyDiscount;
        }

        public boolean hasPromotion() {
            return promotionDiscount > 0.0;
        }

        public boolean hasLoyaltyDiscount() {
            return loyaltyDiscount > 0.0;
        }
    }
}
