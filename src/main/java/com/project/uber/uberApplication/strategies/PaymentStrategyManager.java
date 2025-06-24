package com.project.uber.uberApplication.strategies;

import com.project.uber.uberApplication.entities.enums.PaymentMethod;
import com.project.uber.uberApplication.strategies.impl.CashPaymentStrategy;
import com.project.uber.uberApplication.strategies.impl.WalletPaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStrategyManager {

    private final WalletPaymentStrategy walletPaymentStrategy;
    private final CashPaymentStrategy cashPaymentStrategy;

    public PaymentStrategy paymentStrategy(PaymentMethod paymentMethod){
        return switch (paymentMethod){
            case WALLET -> walletPaymentStrategy;
            case CASH -> cashPaymentStrategy;
            default -> throw new RuntimeException("Invalid Payment Method");
        };
    }

}
