package com.modusbox.client.processor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SetPropertiesLoanInfo implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        String body = exchange.getIn().getBody(String.class);

        JSONObject loanDetails = new JSONObject(body);
        String currentDueDate=null;
        String pastdueDate=null;
        String nextinstallationDate =null;

        if(loanDetails.getJSONArray("data").length()>0)
        {
            if(!loanDetails.getJSONArray("data").getJSONObject(0).isNull("DUE_DATE"))
                currentDueDate = loanDetails.getJSONArray("data").getJSONObject(0).getString("DUE_DATE");

            if(!loanDetails.getJSONArray("data").getJSONObject(0).isNull("PAST_DUE_DATE"))
                pastdueDate = loanDetails.getJSONArray("data").getJSONObject(0).getString("PAST_DUE_DATE");

            if(!loanDetails.getJSONArray("data").getJSONObject(0).isNull("NEXT_INSTALLATION_DATE"))
                nextinstallationDate = loanDetails.getJSONArray("data").getJSONObject(0).getString("NEXT_INSTALLATION_DATE");

            Integer dueAmount = loanDetails.getJSONArray("data").getJSONObject(0).getInt("DUE_AMOUNT");
            Integer pastdueAmount = loanDetails.getJSONArray("data").getJSONObject(0).getInt("Past_Due_Amount");
            Integer nextinstallationAmount = loanDetails.getJSONArray("data").getJSONObject(0).getInt("NEXT_INSTALLATION_AMOUNT");

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date finalDueDate = new Date();
            Integer finalDueAmount = 0;

            if(dueAmount > 0 || pastdueAmount > 0)
            {
                if(currentDueDate != null && dueAmount > 0)
                {
                    finalDueDate = dateFormatter.parse(currentDueDate);
                }
                else
                {
                    finalDueDate = dateFormatter.parse(pastdueDate);
                }
                finalDueAmount = dueAmount + pastdueAmount;
            }
            else
            {
                finalDueDate = dateFormatter.parse(nextinstallationDate);
                finalDueAmount = nextinstallationAmount;
            }

            exchange.setProperty("mfidueDate", dateFormatter.format(finalDueDate));
            exchange.setProperty("mfidueAmount", finalDueAmount);
        }
    }
}
