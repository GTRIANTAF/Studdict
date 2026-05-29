package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.repository.BillRepository;
import com.studdict.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private BillRepository billRepository;
    @Mock private PaymentRepository paymentRepository;
    @InjectMocks private PaymentService paymentService;

    @Test
    void testProcessPayment_AlreadySettled_ThrowsException() {
        // Σενάριο: Προσπαθούμε να πληρώσουμε έναν λογαριασμό που έχει ήδη πληρωθεί
        Bill bill = new Bill();
        bill.setSettled(true);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(1L, "CASH", 50.0);
        });
    }

    @Test
    void testProcessPayment_InsufficientAmount_ThrowsException() {
        // Σενάριο: Το ποσό πληρωμής είναι μικρότερο από το total_amount
        Bill bill = new Bill();
        bill.setTotalAmount(100.0);
        bill.setSettled(false);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(1L, "CASH", 50.0);
        });
    }
}