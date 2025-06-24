package com.project.uber.uberApplication.strategies;

import com.project.uber.uberApplication.entities.Payment;

public interface PaymentStrategy {

    Double PLATFORM_COMMISSION = 0.3;
    void processPayment(Payment payment);
}
