package com.surepay.transactionvalidator.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surepay.transactionvalidator.model.TransactionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JsonTransactionMapperStreamed implements TransactionFileParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<TransactionRecord> parse(MultipartFile file) throws Exception {
        List<TransactionRecord> transactions = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            JsonParser parser = objectMapper.getFactory().createParser(inputStream);

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Expected JSON array");
            }

            while (parser.nextToken() != JsonToken.END_ARRAY) {
                // TODO
                // 💡 To avoid OutOfMemoryError for large files:
                //    Consider validating each record inside this loop and collecting only those that are needed.
                TransactionRecord record = objectMapper.readValue(parser, TransactionRecord.class);
                transactions.add(record);
            }
        }

        log.info("PROCESSING FINISHED");

        return transactions;
    }


    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.endsWith(".json");
    }
}
