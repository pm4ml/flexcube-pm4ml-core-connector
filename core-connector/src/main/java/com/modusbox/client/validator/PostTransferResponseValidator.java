package com.modusbox.client.validator;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;

import java.util.Map;

public class PostTransferResponseValidator implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        if(DataFormatUtils.isJSONValid(body)) {
            JSONObject respObject = new JSONObject(body);

            if (!respObject.has("STLREFNO")) {
                throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "Cannot made loan repayment process"));
            }
        }
        else{
            throw new CCCustomException(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "Cannot made loan repayment process"));
        }
    }
}

