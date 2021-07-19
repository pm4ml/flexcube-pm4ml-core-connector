package com.modusbox.client.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.json.JSONObject;

import java.util.Map;

public class BodyChecker implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        JSONObject respObject = new JSONObject(body);

        if (respObject.has("status")) {
            String status = respObject.getString("status");
            if (!status.equals("SUCCESS")) {
                String uri = "";
                int statusCode = respObject.getInt("code");
                String statusText = status;
                String redirectLocation = "";
                Map<String, String> responseHeaders = null;
                String responseBody = body;

                throw new HttpOperationFailedException(uri, statusCode, statusText, redirectLocation, responseHeaders, responseBody);
            }
        }
    }
}
