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

        //Check the MOBILE_NUMBER and ACCOUNT_NUMBER field is exists or not in result loan info from CBS
        JSONObject fieldName = customerDetails.getJSONArray("data").getJSONObject(0);
        if(fieldName.has("MOBILE_NUMBER") && fieldName.has("ACCOUNT_NUMBER") ) {
            mfiPhoneNumber = customerDetails.getJSONArray("data").getJSONObject(0).getString("MOBILE_NUMBER").replaceAll("[-_+:;|!@$%.,/?^]*","");
            mfiAccountNumber = customerDetails.getJSONArray("data").getJSONObject(0).getString("ACCOUNT_NUMBER");
        }

        // Get the walletPhoneNumber and walletLoanNumber
        String walletPhoneNumber =(String) exchange.getIn().getHeader("idSubValue");
        walletPhoneNumber = walletPhoneNumber.replaceAll("[-_+:;|!@$%.,/?^]*","");
        String walletLoanNumber = (String) exchange.getIn().getHeader("idValue");
        String mfiPrefix = walletLoanNumber.substring(0,3);
        walletLoanNumber = mfiPrefix + (String)exchange.getProperty("loanAccount");

        //For checking the phone number that start with +,95,9 etc..
        walletPhoneNumber = Utility.stripMyanmarPhoneNumberCode(walletPhoneNumber);
        mfiPhoneNumber = Utility.stripMyanmarPhoneNumberCode(mfiPhoneNumber);

        //Get phone number that removed special characters for data sonnet
        exchange.setProperty("walletPhoneNumber", walletPhoneNumber);
        exchange.setProperty("walletLoanNumber", walletLoanNumber);

        if(!Utility.isPhoneNumberMatch(walletPhoneNumber.trim(), mfiPhoneNumber.trim()) || !walletLoanNumber.substring(3).equals(mfiAccountNumber)) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PHONE_NUMBER_MISMATCH));
        }
    }
}
