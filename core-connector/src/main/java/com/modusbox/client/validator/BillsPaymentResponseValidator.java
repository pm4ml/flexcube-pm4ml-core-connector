package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.enums.ErrorCode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

public class BillsPaymentResponseValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        JSONObject respObject = new JSONObject(body);

        if (respObject.has("status")) {
            String status = respObject.getString("status");
            if (!status.equals("SUCCESS")) {
                String errorCode = respObject.getJSONObject("result").getString("errorCode");
                String errorReason = respObject.getJSONObject("result").getString("errorReason");

                if(errorCode.equals("FBF002")) {

                    throw new CCCustomException(ErrorCode.getErrorResponse(
                            ErrorCode.DUPLICATE_REFERENCE_ID,
                            errorReason
                    ));
                } else {
                    throw new Exception();
                }
            }
        }
    }
}