package com.modusbox.client.validator;

import com.modusbox.client.customexception.InvalidAccountNumberException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.json.JSONObject;

import java.util.Map;

public class GetPartyResponseValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        JSONObject respObject = new JSONObject(body);

        if (respObject.has("status")) {
            String status = respObject.getString("status");
            if (!status.equals("SUCCESS")) {

                String errorReason = respObject.getJSONObject("result").getString("errorReason");

                throw new InvalidAccountNumberException(errorReason);
            }
        }
    }
}