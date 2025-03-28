package com.surepay.transactionvalidator.service;

import com.surepay.transactionvalidator.model.TransactionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class TransactionValidatorServiceImpl implements TransactionValidatorService {


    public List<TransactionRecord> validateTransactions(List<TransactionRecord> transactions) {
        Set<Long> uniqueReferences = new HashSet<>();
        List<TransactionRecord> invalidRecords = new ArrayList<>();

        for (TransactionRecord record : transactions) {
            boolean isDuplicateReference = !uniqueReferences.add(record.getReference());
            boolean isEndBalanceIncorrect = !isEndBalanceValid(record);

            StringBuilder reason = new StringBuilder();
            if (isDuplicateReference) {
                reason.append("[ERROR: Duplicate transaction reference] ");
            }
            if (isEndBalanceIncorrect) {
                reason.append("[ERROR: Incorrect end balance, expected ")
                        .append(record.getStartBalance().add(record.getMutation()))
                        .append("] ");
            }

            if (!reason.isEmpty()) {
                String reasonText = reason.toString().trim();

                log.error("Invalid transaction detected: reference={}, account={}, reason={}",
                        record.getReference(), record.getAccountNumber(), reasonText);

                record.setDescription(
                        Optional.ofNullable(record.getDescription()).orElse("") + " " + reasonText
                );

                invalidRecords.add(record);
            }
        }

        return invalidRecords;
    }

    private boolean isEndBalanceValid(TransactionRecord record) {
        BigDecimal expectedEndBalance = record.getStartBalance().add(record.getMutation());
        return expectedEndBalance.compareTo(record.getEndBalance()) == 0;
    }
}