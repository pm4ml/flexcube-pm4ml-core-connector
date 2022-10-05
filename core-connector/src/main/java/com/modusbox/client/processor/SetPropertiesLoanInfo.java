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

        if(!loanDetails.isNull("dueDate"))
            currentDueDate = loanDetails.getString("dueDate");

        if(!loanDetails.isNull("pastDueDate"))
            pastdueDate = loanDetails.getString("pastDueDate");

        if(!loanDetails.isNull("nextInstallationDate"))
            nextinstallationDate = loanDetails.getString("nextInstallationDate");

        Integer dueAmount = loanDetails.getInt("dueAmount");
        Integer pastdueAmount = loanDetails.getInt("pastDueAmount");
        Integer nextinstallationAmount = loanDetails.getInt("nextInstallationAmount");

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
