package com.project.uber.uberApplication.strategies.impl;

import com.project.uber.uberApplication.entities.Driver;
import com.project.uber.uberApplication.entities.Payment;
import com.project.uber.uberApplication.entities.Rider;
import com.project.uber.uberApplication.entities.enums.PaymentStatus;
import com.project.uber.uberApplication.entities.enums.TransactionMethod;
import com.project.uber.uberApplication.repositories.PaymentRepository;
import com.project.uber.uberApplication.services.WalletService;
import com.project.uber.uberApplication.strategies.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletPaymentStrategy implements PaymentStrategy {

    private final WalletService walletService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void processPayment(Payment payment) {
        Driver driver = payment.getRide().getDriver();
        Rider rider = payment.getRide().getRider();

        walletService.deductMoneyFromWallet(rider.getUser(), payment.getAmount(), null, payment.getRide(), TransactionMethod.RIDE);

        double driversCut = payment.getAmount() * (1-PLATFORM_COMMISSION);

        walletService.addMoneyToWallet(driver.getUser(), driversCut, null, payment.getRide(), TransactionMethod.RIDE);

        payment.setPaymentStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);
    }
}
