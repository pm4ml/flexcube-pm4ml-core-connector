package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class SettledBalanceValidator implements Processor {


    @Override
    public void process(Exchange exchange) throws Exception {
        String accountNumber = (String) exchange.getProperty("mfiLoanAccountNo");
        String settledAmount = (String) exchange.getProperty("transactionAmount");

        if((accountNumber == null || accountNumber.trim().isEmpty()) || (settledAmount == null || settledAmount.trim().isEmpty())) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.MISSING_MANDATORY_ELEMENT,"Required field missing"));
        }

        if(!DataFormatUtils.isOnlyDigits(accountNumber) )
        {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.MALFORMED_SYNTAX,"Invalid Account Number Format"));
        }
        if(!DataFormatUtils.isOnlyDigits(settledAmount))
        {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.ROUNDING_VALUE_ERROR,"Invalid settled amount Format"));
        }

        if (settledAmount.equals("0")) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR, "Transfer amount cannot be zero value"));
        }
    }
}
