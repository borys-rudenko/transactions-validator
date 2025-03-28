package com.surepay.transactionvalidator.mapper;

import com.surepay.transactionvalidator.model.TransactionRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CsvTransactionMapperStreamed implements TransactionFileParser {

    @Override
    public List<TransactionRecord> parse(MultipartFile file) throws Exception {
        List<TransactionRecord> transactions = new ArrayList<>();
        CSVFormat format = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 16 * 1024  // 16 KB buffer
                );
                CSVParser csvParser = new CSVParser(reader, format)
        ) {
            for (CSVRecord csvRecord : csvParser) {
                // TODO
                // 💡 To avoid OutOfMemoryError for large files:
                //    Consider validating each record inside this loop and collecting only those that are needed.
                transactions.add(mapRecord(csvRecord));
            }
            log.info("PROCESSING FINISHED");
        }

        return transactions;
    }

    private TransactionRecord mapRecord(CSVRecord csvRecord) {
        TransactionRecord transaction = new TransactionRecord();
        transaction.setReference(Long.parseLong(csvRecord.get("Reference")));
        transaction.setAccountNumber(csvRecord.get("AccountNumber"));
        transaction.setDescription(csvRecord.get("Description"));
        transaction.setStartBalance(new BigDecimal(csvRecord.get("Start Balance")));
        transaction.setMutation(new BigDecimal(csvRecord.get("Mutation")));
        transaction.setEndBalance(new BigDecimal(csvRecord.get("End Balance")));
        return transaction;
    }

    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.endsWith(".csv");
    }
}
