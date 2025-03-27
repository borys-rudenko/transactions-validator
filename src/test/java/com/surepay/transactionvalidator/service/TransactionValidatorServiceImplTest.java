package com.surepay.transactionvalidator.service;

import com.surepay.transactionvalidator.model.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionValidatorServiceImplTest {

    private TransactionValidatorServiceImpl validatorService;

    @BeforeEach
    void setUp() {
        validatorService = new TransactionValidatorServiceImpl();
    }

    @Test
    void shouldReturnEmptyListWhenAllTransactionsAreValid() {
        TransactionRecord record = new TransactionRecord();
        record.setReference(1L);
        record.setAccountNumber("NL91ABNA0417164300");
        record.setStartBalance(new BigDecimal("100.00"));
        record.setMutation(new BigDecimal("-20.00"));
        record.setEndBalance(new BigDecimal("80.00"));
        record.setDescription("Valid Transaction");

        List<TransactionRecord> result = validatorService.validateTransactions(List.of(record));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDetectDuplicateReference() {
        TransactionRecord record1 = createValidRecord(1L);
        TransactionRecord record2 = createValidRecord(1L); // same reference

        List<TransactionRecord> result = validatorService.validateTransactions(List.of(record1, record2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReference()).isEqualTo(1L);
        assertThat(result.get(0).getDescription()).contains("Duplicate transaction reference");
    }

    @Test
    void shouldDetectIncorrectEndBalance() {
        TransactionRecord record = new TransactionRecord();
        record.setReference(2L);
        record.setAccountNumber("NL91ABNA0417164301");
        record.setStartBalance(new BigDecimal("100.00"));
        record.setMutation(new BigDecimal("-20.00"));
        record.setEndBalance(new BigDecimal("85.00")); // incorrect
        record.setDescription("Incorrect balance");

        List<TransactionRecord> result = validatorService.validateTransactions(List.of(record));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReference()).isEqualTo(2L);
        assertThat(result.get(0).getDescription()).contains("Incorrect end balance");
    }

    @Test
    void shouldDetectBothErrors() {
        TransactionRecord record1 = createValidRecord(3L);
        TransactionRecord record2 = createValidRecord(3L);
        record2.setEndBalance(new BigDecimal("999.99")); // wrong balance

        List<TransactionRecord> result = validatorService.validateTransactions(List.of(record1, record2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReference()).isEqualTo(3L);
        assertThat(result.get(0).getDescription()).contains("Duplicate transaction reference")
                .contains("Incorrect end balance");
    }

    private TransactionRecord createValidRecord(Long reference) {
        TransactionRecord record = new TransactionRecord();
        record.setReference(reference);
        record.setAccountNumber("NL91ABNA0417164300");
        record.setStartBalance(new BigDecimal("100.00"));
        record.setMutation(new BigDecimal("-20.00"));
        record.setEndBalance(new BigDecimal("80.00"));
        record.setDescription("Test transaction");
        return record;
    }
}
