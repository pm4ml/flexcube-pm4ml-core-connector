package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

import java.util.Map;

public class PostTransferRequestValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        if(DataFormatUtils.isJSONValid(body)) {
            JSONObject respObject = new JSONObject(body);

            if (!respObject.has("TransactionID") || !respObject.has("MakerUserID")|| !respObject.has("AccountNumber") || !respObject.has("SettledAmount")|| !respObject.has("SetlledGL")) {
                throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.MISSING_MANDATORY_ELEMENT, "Required field missing"));
            }
        }
        else{
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.MISSING_MANDATORY_ELEMENT, "Required field missing"));
        }
    }
}

