package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.enums.ErrorCode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
                String errorCode = respObject.getJSONObject("result").getString("errorCode");
                String errorReason = respObject.getJSONObject("result").getString("errorReason");

               if(errorCode.equals("FBF006")) {
                   throw new CloseWrittenOffAccountException(errorReason);
               } else if(errorCode.equals("FBF004")) {
                   throw new CCCustomException(ErrorCode.getErrorResponse(
                           ErrorCode.GENERIC_ID_NOT_FOUND,
                           errorReason
                   ));
               } else {
                   throw new CCCustomException(ErrorCode.getErrorResponse(
                           ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE,
                           errorReason
                   ));
               }
            }
        }
    }
}