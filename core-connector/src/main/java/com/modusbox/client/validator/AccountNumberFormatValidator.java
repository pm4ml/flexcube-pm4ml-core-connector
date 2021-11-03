package com.modusbox.client.validator;


import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.regex.Pattern;

public class AccountNumberFormatValidator implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String idType = (String) exchange.getIn().getHeader("idType");
        String loanAccount;
        String phoneNumber;

        if(idType.equalsIgnoreCase("ACCOUNT_ID")) {
            //Valid the loan account number is digit
            loanAccount = (String) exchange.getIn().getHeader("idValue");
            if(!DataFormatUtils.isOnlyDigits(loanAccount)) {
                throw new CCCustomException(ErrorCode.getErrorResponse(
                        ErrorCode.MALFORMED_SYNTAX,
                        "Invalid Account Number Format"));
            }
            //Valid the phone number is digit
            phoneNumber = (String) exchange.getIn().getHeader("idSubValue");
            if(!DataFormatUtils.isOnlyDigits(phoneNumber)) {
                throw new CCCustomException(ErrorCode.getErrorResponse(
                        ErrorCode.MALFORMED_SYNTAX,
                        "Invalid Phone Number Format"));
            }
        }
    }
}