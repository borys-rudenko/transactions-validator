package com.surepay.transactionvalidator.mapper;

import com.surepay.transactionvalidator.model.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvTransactionMapperTest {

    private CsvTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CsvTransactionMapper();
    }

    @Test
    void shouldParseValidCsv() throws Exception {
        String csv = """
                Reference,AccountNumber,Description,Start Balance,Mutation,End Balance
                1001,NL91ABNA0417164300,Grocery,100.00,-20.00,80.00
                1002,NL91ABNA0417164301,Salary,200.00,50.00,250.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        List<TransactionRecord> records = mapper.parse(file);

        assertThat(records).hasSize(2);
        assertThat(records.get(0).getReference()).isEqualTo(1001L);
        assertThat(records.get(0).getDescription()).isEqualTo("Grocery");
        assertThat(records.get(1).getMutation()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldThrowExceptionWhenColumnMissing() {
        String csv = """
                Reference,AccountNumber,Start Balance,Mutation,End Balance
                1001,NL91ABNA0417164300,100.00,-20.00,80.00
                """; // пропущен Description

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        Exception ex = assertThrows(Exception.class, () -> mapper.parse(file));
        assertThat(ex.getMessage()).contains("Mapping for Description not found, expected one of [Reference, AccountNumber, Start Balance, Mutation, End Balance]");
    }

    @Test
    void shouldSupportCsvExtension() {
        assertThat(mapper.supports("transactions.csv")).isTrue();
    }

    @Test
    void shouldNotSupportNonCsvExtension() {
        assertThat(mapper.supports("transactions.json")).isFalse();
    }
}
