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

class JsonTransactionMapperTest {

    private JsonTransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JsonTransactionMapper();
    }

    @Test
    void shouldParseValidJson() throws Exception {
        String json = """
            [
              {
                "reference": 1001,
                "accountNumber": "NL91ABNA0417164300",
                "description": "Grocery",
                "startBalance": 100.00,
                "mutation": -20.00,
                "endBalance": 80.00
              },
              {
                "reference": 1002,
                "accountNumber": "NL91ABNA0417164301",
                "description": "Salary",
                "startBalance": 200.00,
                "mutation": 50.00,
                "endBalance": 250.00
              }
            ]
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "transactions.json", "application/json", json.getBytes(StandardCharsets.UTF_8)
        );

        List<TransactionRecord> records = mapper.parse(file);

        assertThat(records).hasSize(2);
        assertThat(records.get(0).getReference()).isEqualTo(1001L);
        assertThat(records.get(1).getEndBalance()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        String invalidJson = """
            { "this is": "not valid list json" }
        """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.json", "application/json", invalidJson.getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(Exception.class, () -> mapper.parse(file));
    }

    @Test
    void supportsShouldReturnTrueForJsonFile() {
        assertThat(mapper.supports("transactions.json")).isTrue();
    }

    @Test
    void supportsShouldReturnFalseForNonJsonFile() {
        assertThat(mapper.supports("transactions.csv")).isFalse();
    }
}
