package com.houndjo.infrastructure.adapter.in.rest.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO containing Stripe PaymentIntent data needed by the frontend to confirm payment.
 */
@Schema(name = "StripePaymentIntent")
public record StripePaymentIntentDTO(
        /** Stripe client_secret — pass to stripe.confirmCardPayment() on the frontend. */
        String clientSecret,
        /** Stripe PaymentIntent ID (pi_...) — send back with POST /subscriptions to finalize. */
        String paymentIntentId,
        /** Amount in the currency's smallest unit (e.g. cents). */
        long amountInSmallestUnit,
        /** ISO 4217 currency code in lowercase (e.g. "usd"). */
        String currency) {}
