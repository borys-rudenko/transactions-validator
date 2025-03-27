package com.surepay.transactionvalidator.service;

import com.surepay.transactionvalidator.model.TransactionRecord;

import java.util.List;

public interface TransactionValidatorService {

    /**
     * Validates a list of transaction records.
     *
     * @param transactions The list of transactions to validate.
     * @return List of invalid transactions.
     */
    List<TransactionRecord> validateTransactions(List<TransactionRecord> transactions);
}
