package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.PhoneNumberUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhoneNumberValidation implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        String mfiPhoneNumber = "";
        String mfiAccountNumber = "";
        String body = exchange.getIn().getBody(String.class);

        JSONObject customerDetails = new JSONObject(body);

        //Check the MOBILE_NUMBER field is exists or not
        boolean isExistPhNoField = false;
        boolean isExistAccountNoField = false;

        for (int i = 0; i < customerDetails.getJSONArray("data").length(); i++) {
            JSONObject fieldName = customerDetails.getJSONArray("data").getJSONObject(i);
            if (fieldName.has("MOBILE_NUMBER")) {
                //  Exist
                isExistPhNoField = true;
            }
            if (fieldName.has("ACCOUNT_NUMBER")) {
                //  Exist
                isExistAccountNoField = true;
            }
        }

        //Get the mfiPhoneNumber and mfiAccountNumber from MFIResponse data,if both fields are exists
        if(isExistPhNoField && isExistAccountNoField) {
            mfiPhoneNumber = customerDetails.getJSONArray("data").getJSONObject(0).getString("MOBILE_NUMBER").replaceAll("[-_+:;|!@$%.,/?^]*","");
            mfiAccountNumber = customerDetails.getJSONArray("data").getJSONObject(0).getString("ACCOUNT_NUMBER");
        }

        // Get the walletPhoneNumber from idSubValue
        //String idSubvalue = (String) exchange.getIn().getHeader("idSubValue");
        String walletPhoneNumber =(String) exchange.getIn().getHeader("idSubValue");
        walletPhoneNumber = walletPhoneNumber.replaceAll("[-_+:;|!@$%.,/?^]*","");

        String walletLoanNumber =
                (String) exchange.getIn().getHeader("idValue");

        //For checking the phone number that start with +,95,9 etc..
        walletPhoneNumber = PhoneNumberUtils.stripCode(walletPhoneNumber);
        mfiPhoneNumber = PhoneNumberUtils.stripCode(mfiPhoneNumber);

        //Get phone number that removed special characters for data sonnet
        exchange.setProperty("mfiPhoneNumber", mfiPhoneNumber);
        String dueDate=null;
        String pastdueDate=null;
        String nextinstallationDate =null;

        if(customerDetails.getJSONArray("data").length()>0)
        {
            if(!customerDetails.getJSONArray("data").getJSONObject(0).isNull("DUE_DATE"))
                dueDate = customerDetails.getJSONArray("data").getJSONObject(0).getString("DUE_DATE");

            if(!customerDetails.getJSONArray("data").getJSONObject(0).isNull("PAST_DUE_DATE"))
                pastdueDate = customerDetails.getJSONArray("data").getJSONObject(0).getString("PAST_DUE_DATE");

            if(!customerDetails.getJSONArray("data").getJSONObject(0).isNull("NEXT_INSTALLATION_DATE"))
                nextinstallationDate = customerDetails.getJSONArray("data").getJSONObject(0).getString("NEXT_INSTALLATION_DATE");


            Double dueAmount = customerDetails.getJSONArray("data").getJSONObject(0).getDouble("DUE_AMOUNT");
            Double pastdueAmount = customerDetails.getJSONArray("data").getJSONObject(0).getDouble("Past_Due_Amount");
            Double nextinstallationAmount = customerDetails.getJSONArray("data").getJSONObject(0).getDouble("NEXT_INSTALLATION_AMOUNT");

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date temDueDate = new Date();

            if(!dueAmount.equals(0) || !pastdueAmount.equals(0))
            {
                if(pastdueDate == null && dueDate != null)
                {
                    temDueDate = dateFormatter.parse(dueDate);
                }
                if(pastdueDate != null && dueDate == null)
                {
                    temDueDate = dateFormatter.parse(pastdueDate);
                }
            }
            else
            {
                temDueDate = dateFormatter.parse(nextinstallationDate);
            }
            exchange.setProperty("mfidueDate", dateFormatter.format(temDueDate));
        }

        if(!PhoneNumberUtils.isPhoneNumberMatch(walletPhoneNumber.trim(), mfiPhoneNumber.trim()) || !walletLoanNumber.substring(3).equals(mfiAccountNumber)) {
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.PHONE_NUMBER_MISMATCH));
        }
    }
}
