package com.surepay.transactionvalidator.controller;

import com.surepay.transactionvalidator.mapper.TransactionFileParser;
import com.surepay.transactionvalidator.model.TransactionRecord;
import com.surepay.transactionvalidator.service.TransactionValidatorServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionsController.class)
class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionValidatorServiceImpl validatorService;

    @MockBean
    private TransactionFileParser csvParser;

    @Test
    void shouldProcessCsvFileAndReturnValidatedTransactions() throws Exception {
        String csv = """
                Reference,AccountNumber,Description,Start Balance,Mutation,End Balance
                1001,NL91ABNA0417164300,Grocery,100.00,-20.00,80.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "transactions.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        // Parsed transaction
        TransactionRecord parsed = new TransactionRecord();
        parsed.setReference(1001L);
        parsed.setAccountNumber("NL91ABNA0417164300");
        parsed.setDescription("Grocery");
        parsed.setStartBalance(new BigDecimal("100.00"));
        parsed.setMutation(new BigDecimal("-20.00"));
        parsed.setEndBalance(new BigDecimal("80.00"));

        // Mocking parser support
        when(csvParser.supports("transactions.csv")).thenReturn(true);
        when(csvParser.parse(any())).thenReturn(List.of(parsed));
        when(validatorService.validateTransactions(any())).thenReturn(List.of(parsed));

        // Run test
        mockMvc.perform(multipart("/transactions/validate")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference").value(1001))
                .andExpect(jsonPath("$[0].description").value("Grocery"));
    }

    @Test
    void shouldReturnBadRequestForUnsupportedFileFormat() throws Exception {
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file", "invalid.txt", "text/plain", "some content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/transactions/validate").file(unsupportedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported file format: invalid.txt"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenParserFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "transactions.csv", "text/csv", "bad content".getBytes(StandardCharsets.UTF_8)
        );

        when(csvParser.supports("transactions.csv")).thenReturn(true);
        when(csvParser.parse(any())).thenThrow(new RuntimeException("Parsing failed"));

        mockMvc.perform(multipart("/transactions/validate").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parsing failed"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        String csv = """
            Reference,AccountNumber,Description,Start Balance,Mutation,End Balance
            1001,NL91ABNA0417164300,Grocery,100.00,-20.00,80.00
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "transactions.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        TransactionRecord record = new TransactionRecord();
        record.setReference(1001L);

        when(csvParser.supports("transactions.csv")).thenReturn(true);
        when(csvParser.parse(any())).thenReturn(List.of(record));
        when(validatorService.validateTransactions(any()))
                .thenThrow(new IllegalArgumentException("Validation failed"));

        mockMvc.perform(multipart("/transactions/validate").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400));
    }

}
