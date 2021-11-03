package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

public class SettlementAmountValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Integer repaymentSettledAmount;
        repaymentSettledAmount = Integer.parseInt ((String) exchange.getProperty("transferAmount"));

        if (repaymentSettledAmount <=0) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR, "Transfer amount cannot be zero value"));
        }
    }
}