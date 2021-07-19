package com.modusbox.client.processor;

import com.modusbox.log4j2.message.CustomJsonMessage;
import com.modusbox.log4j2.message.CustomJsonMessageImpl;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component("customErrorProcessor")
public class ErrorHandler implements Processor {

    private static final CustomJsonMessage LOGGER = new CustomJsonMessageImpl();

    @Override
    public void process(Exchange exchange) throws Exception {

        int httpResponseCode = 500;
        String reasonText = "Unknown";
        String statusCode = String.valueOf(httpResponseCode);

        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        if (exception != null) {

            reasonText = exception.getMessage();

            if (exception instanceof HttpOperationFailedException) {
                HttpOperationFailedException e = (HttpOperationFailedException) exception;
                httpResponseCode = e.getStatusCode();
                statusCode = String.valueOf(httpResponseCode);
                reasonText = "Downstream API failed";
                try {
                    if (null != e.getResponseBody()) {
                        JSONObject respObject = new JSONObject(e.getResponseBody());
                        if (respObject.has("returnStatus")) {
                            statusCode = String.valueOf(respObject.getInt("returnCode"));
                            reasonText = respObject.getString("returnStatus");
                        } else if (respObject.has("error_description")) {
                            reasonText = String.format("%s: %s",
                                    respObject.getString("error"),
                                    respObject.getString("error_description"));
                        } else if (respObject.has("result")) {
                            reasonText = String.format("%s: %s",
                                    respObject.getJSONObject("result").getString("errorCode"),
                                    respObject.getJSONObject("result").getString("errorReason"));
                        }
                    }
                } catch (Exception e2) {
                    LOGGER.logJsonMessage(
                            "error",
                            String.valueOf(exchange.getIn().getHeader("X-CorrelationId")),
                            "Exception at CustomErrorProcessor",
                            null,
                            null,
                            e2.getMessage());
                }
            }

            LOGGER.logJsonMessage(
                    "error",
                    String.valueOf(exchange.getIn().getHeader("X-CorrelationId")),
                    "Processing the exception at CustomErrorProcessor",
                    null,
                    null,
                    exception.getMessage());
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(String.format("{ \"code\": \"%s\",\"message\": \"%s\" }", statusCode, reasonText));
    }
}
