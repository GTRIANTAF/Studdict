package com.studdict.service;

import com.studdict.model.LoyaltyWallet;
import com.studdict.repository.LoyaltyWalletRepository;
import com.studdict.repository.PointsTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GamificationServiceTest {

    @Mock private LoyaltyWalletRepository walletRepository;
    @Mock private PointsTransactionRepository transactionRepository;
    @InjectMocks private GamificationService gamificationService;

    @Test
    void testGetWallet_ExistingWallet_ReturnsWallet() {
        LoyaltyWallet wallet = new LoyaltyWallet("S1");
        wallet.setTotalBalance(150);
        when(walletRepository.findById("S1")).thenReturn(Optional.of(wallet));

        LoyaltyWallet result = gamificationService.getWallet("S1");

        assertNotNull(result);
        assertEquals("S1", result.getStudentId());
        assertEquals(150, result.getTotalBalance());
    }

    @Test
    void testRedeemPoints_InsufficientBalance_ReturnsFalse() {
        LoyaltyWallet wallet = new LoyaltyWallet("S1");
        wallet.setTotalBalance(50); // Less than 100 (minimum limit)
        when(walletRepository.findById("S1")).thenReturn(Optional.of(wallet));

        boolean success = gamificationService.redeemPoints("S1", 100);

        assertFalse(success);
        assertEquals(50, wallet.getTotalBalance());
    }

    @Test
    void testRedeemPoints_SufficientBalance_DeductsPointsAndReturnsTrue() {
        LoyaltyWallet wallet = new LoyaltyWallet("S1");
        wallet.setTotalBalance(250);
        when(walletRepository.findById("S1")).thenReturn(Optional.of(wallet));

        boolean success = gamificationService.redeemPoints("S1", 100);

        assertTrue(success);
        assertEquals(150, wallet.getTotalBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void testCreditPointsForStudy_AwardPoints() {
        LoyaltyWallet wallet = new LoyaltyWallet("S1");
        wallet.setTotalBalance(10);
        when(walletRepository.findById("S1")).thenReturn(Optional.of(wallet));

        int pointsEarned = gamificationService.creditPointsForStudy("S1", 60); // Flat 50 points per checkout

        assertEquals(50, pointsEarned);
        assertEquals(60, wallet.getTotalBalance());
        verify(walletRepository, times(1)).save(wallet);
    }
}