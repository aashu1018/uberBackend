package com.project.uber.uberApplication.strategies.impl;

import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.Payment;
import com.project.uber.uberApplication.entities.enums.PaymentStatus;
import com.project.uber.uberApplication.entities.enums.TransactionMethod;
import com.project.uber.uberApplication.repositories.PaymentRepository;
import com.project.uber.uberApplication.services.WalletService;
import com.project.uber.uberApplication.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CashPaymentStrategy implements PaymentStrategy {

    private final WalletService walletService;
    private final PaymentRepository paymentRepository;

    @Override
    public void processPayment(Payment payment) {
        Driver driver = payment.getRide().getDriver();

        double platformCommission = payment.getAmount()*PLATFORM_COMMISSION;

        walletService.deductMoneyFromWallet(driver.getUser(), platformCommission, null, payment.getRide(), TransactionMethod.RIDE);

        payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);
    }
}
