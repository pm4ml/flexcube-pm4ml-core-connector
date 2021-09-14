package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.PhoneNumberUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

public class PhoneNumberValidation implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String mfiPhoneNumber = "";
        String body = exchange.getIn().getBody(String.class);
        JSONObject respObject = new JSONObject(body);
        JSONObject customerDetails = respObject.getJSONObject("result").getJSONObject("customerDetails");
        if(customerDetails.has("mobileNo")) {
            mfiPhoneNumber =
                    customerDetails.getString("mobileNo");
        }

        String walletPhoneNumber =
                (String) exchange.getIn().getHeader("idSubValue");
        if(!PhoneNumberUtils.isPhoneNumberMatch(walletPhoneNumber.trim(), mfiPhoneNumber.trim())) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PHONE_NUMBER_MISMATCH));
        }
    }
}
