package com.surepay.transactionvalidator.mapper;

import com.surepay.transactionvalidator.model.TransactionRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TransactionFileParser {
    List<TransactionRecord> parse(MultipartFile file) throws Exception;

    boolean supports(String fileName);
}
