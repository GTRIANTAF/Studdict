package com.studdict.repository;

import com.studdict.model.LoyaltyWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyWalletRepository extends JpaRepository<LoyaltyWallet, String> {
}