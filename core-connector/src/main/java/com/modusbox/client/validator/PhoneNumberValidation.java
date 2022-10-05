package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import com.modusbox.client.utils.Utility;

public class PhoneNumberValidation implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        String mfiPhoneNumber = "";
        String mfiAccountNumber = "";
        String body = exchange.getIn().getBody(String.class);

        JSONObject customerDetails = new JSONObject(body);

        if(customerDetails.has("mobileNumber") && customerDetails.has("accuntNumber") ) {
            mfiPhoneNumber = customerDetails.getString("mobileNumber").replaceAll("[-_+:;|!@$%.,/?^]*","");
            mfiAccountNumber = customerDetails.getString("accuntNumber");
        }

        // Get the walletPhoneNumber and walletLoanNumber
        String walletPhoneNumber =(String) exchange.getIn().getHeader("idSubValue");
        walletPhoneNumber = walletPhoneNumber.replaceAll("[-_+:;|!@$%.,/?^]*","");
        String walletLoanNumber = exchange.getProperty("loanAccount").toString();//(String) exchange.getIn().getHeader("idValue");

        //For checking the phone number that start with +,95,9 etc..
        walletPhoneNumber = Utility.stripMyanmarPhoneNumberCode(walletPhoneNumber);
        mfiPhoneNumber = Utility.stripMyanmarPhoneNumberCode(mfiPhoneNumber);

        //Get phone number that removed special characters for data sonnet
        exchange.setProperty("walletPhoneNumber", walletPhoneNumber);
        exchange.setProperty("walletLoanNumber", exchange.getIn().getHeader("idValue"));

        if(!Utility.isPhoneNumberMatch(walletPhoneNumber.trim(), mfiPhoneNumber.trim()) || !walletLoanNumber.equals(mfiAccountNumber)) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PHONE_NUMBER_MISMATCH));
        }
    }
}
