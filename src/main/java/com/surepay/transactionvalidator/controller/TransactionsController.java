package com.surepay.transactionvalidator.controller;

import com.surepay.transactionvalidator.api.TransactionsApi;
import com.surepay.transactionvalidator.mapper.TransactionFileParser;
import com.surepay.transactionvalidator.model.TransactionRecord;
import com.surepay.transactionvalidator.service.TransactionValidatorServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
public class TransactionsController implements TransactionsApi {

    private final TransactionValidatorServiceImpl validatorService;
    private final List<TransactionFileParser> transactionParsers;

    public TransactionsController(TransactionValidatorServiceImpl validatorService,
                                  List<TransactionFileParser> transactionParsers) {
        this.validatorService = validatorService;
        this.transactionParsers = transactionParsers;
    }

    @Override
    public ResponseEntity<List<TransactionRecord>> validateTransactions(MultipartFile file) {
        log.debug("Received file: {}, Size: {} bytes", file.getOriginalFilename(), file.getSize());

        try {
            List<TransactionRecord> transactions = parseFile(file);
            List<TransactionRecord> invalidRecords = validatorService.validateTransactions(transactions);
            return ResponseEntity.ok(invalidRecords);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private List<TransactionRecord> parseFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();

        return transactionParsers.stream()
                .filter(parser -> parser.supports(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file format: " + fileName))
                .parse(file);
    }
}
