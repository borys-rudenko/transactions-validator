package com.surepay.transactionvalidator.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.surepay.transactionvalidator.model.TransactionRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Component
public class JsonTransactionMapper implements TransactionFileParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<TransactionRecord> parse(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        }
    }

    @Override
    public boolean supports(String fileName) {
        return fileName != null && fileName.endsWith(".json");
    }
}
