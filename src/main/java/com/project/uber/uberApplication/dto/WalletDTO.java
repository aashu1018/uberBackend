package com.project.uber.uberApplication.dto;

import com.project.uber.uberApplication.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDTO {

    private Long id;

    private User user;

    private Double balance;

    private List<WalletTransactionDTO> transactions;
}
