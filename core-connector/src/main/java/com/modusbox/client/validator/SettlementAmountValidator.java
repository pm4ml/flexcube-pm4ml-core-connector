package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

public class SettlementAmountValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String repaymentSettledAmount;
        repaymentSettledAmount = (String) exchange.getProperty("transferAmount");

        if(!DataFormatUtils.isOnlyDigits(repaymentSettledAmount))
        {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.ROUNDING_VALUE_ERROR,"Invalid settled amount Format"));
        }

        if (repaymentSettledAmount.equals("0")) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR, "Transfer amount cannot be zero value"));
        }
    }
}