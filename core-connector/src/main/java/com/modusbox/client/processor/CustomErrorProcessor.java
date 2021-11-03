package com.modusbox.client.processor;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.enums.ErrorCode;
import com.modusbox.log4j2.message.CustomJsonMessage;
import com.modusbox.log4j2.message.CustomJsonMessageImpl;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.apache.camel.component.bean.validator.BeanValidationException;

import javax.ws.rs.InternalServerErrorException;
import java.net.SocketTimeoutException;
import com.modusbox.client.utils.DataFormatUtils;
import org.apache.http.conn.HttpHostConnectException;

@Component("customErrorProcessor")
public class CustomErrorProcessor implements Processor {

    CustomJsonMessage customJsonMessage = new CustomJsonMessageImpl();

    @Override
    public void process(Exchange exchange) throws Exception {

        String reasonText = "{ \"statusCode\": \"5000\"," +
                "\"message\": \"Unknown\" }";
        String statusCode = "5000";
        int httpResponseCode = 500;

        JSONObject errorResponse = null;

        String errorDescription = "Downstream API failed.";
        // The exception may be in 1 of 2 places
        Exception exception = exchange.getException();
        if (exception == null) {
            exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        }

        if (exception != null) {
            if (exception instanceof HttpOperationFailedException) {
                HttpOperationFailedException e = (HttpOperationFailedException) exception;
                try {
                    String customCBSMessage = "";
                    if (null != e.getResponseBody() && !e.getResponseBody().isEmpty()) {
                        if(DataFormatUtils.isJSONValid(e.getResponseBody()))
                        {
                            JSONObject respObject = new JSONObject(e.getResponseBody());
                            if(respObject.has("Message")){
                                customCBSMessage = respObject.getString("Message");
                            }

                            if(respObject.has("error")) {
                                customCBSMessage = (respObject.getString("error")).toString() ;
                            }
                        }
                        else{
                         customCBSMessage = e.getResponseBody();}
                    }
                    if(e.getStatusCode() == 406){
                        if(e.getUri().contains("/api/balance")){// Error 406 came from Balance GET REQUEST API method for checking settled amount.
                            errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR,customCBSMessage));}
                        else{// Error 406 came from LOAN POST REQUEST API method for doing loan repayment.
                            errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR,"Loan repayment process cannot be posted")); }
                    }
                    if(e.getStatusCode() == 417) {
                        customCBSMessage = customCBSMessage.isEmpty() ? "Cannot made loan repayment process" : customCBSMessage + ", cannot made loan repayment process";
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,customCBSMessage));
                    }
                    if(e.getStatusCode() == 500){
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,customCBSMessage));
                    }
                    if(e.getStatusCode() == 404){
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR,customCBSMessage));
                    }
                    statusCode =  String.valueOf(errorResponse.getJSONObject("errorInformation").getInt("statusCode"));
                    errorDescription = errorResponse.getJSONObject("errorInformation").getString("description");

                } finally {
                    reasonText = "{ \"statusCode\": \"" + statusCode + "\"," +
                            "\"message\": \"" + errorDescription + "\"} ";
                }
            } else if(exception instanceof CloseWrittenOffAccountException) {
                httpResponseCode = 200;
                reasonText = "{\"idType\": \"" + (String) exchange.getIn().getHeader("idType") +
                        "\",\"idValue\": \"" + (String) exchange.getIn().getHeader("idValue") +
                        "\",\"idSubValue\": \"" + (String) exchange.getIn().getHeader("idSubValue") +
                        "\",\"extensionList\": [{\"key\": \"errorMessage\",\"value\": \"" + exception.getMessage() +
                        "\"}]}";

            }
            else {
                try {
                    if(exception instanceof CCCustomException) {
                        errorResponse = new JSONObject(exception.getMessage());
                    } else if(exception instanceof InternalServerErrorException) {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
                    } else if(exception instanceof ConnectTimeoutException || exception instanceof SocketTimeoutException || exception instanceof HttpHostConnectException) {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.SERVER_TIMED_OUT));
                    } else if (exception instanceof JSONException) {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, exception.getMessage()));
                    } else if (exception instanceof java.lang.Exception || exception instanceof BeanValidationException) {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "CC logical transformation error"));
                    }
                } finally {
                    httpResponseCode = errorResponse.getInt("errorCode");
                    errorResponse = errorResponse.getJSONObject("errorInformation");
                    statusCode = String.valueOf(errorResponse.getInt("statusCode"));
                    errorDescription = errorResponse.getString("description");
                    reasonText = "{ \"statusCode\": \"" + statusCode + "\"," +
                            "\"message\": \"" + errorDescription + "\"} ";
                }
            }
                customJsonMessage.logJsonMessage("error", null,
                    "Processing the exception at CustomErrorProcessor", null, null,
                    exception.getMessage());
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(reasonText);
    }
}