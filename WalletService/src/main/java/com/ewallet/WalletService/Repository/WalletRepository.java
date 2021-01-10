package com.ewallet.WalletService.Repository;

import com.ewallet.WalletService.Model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    Wallet findByUserId(int userId);
}
