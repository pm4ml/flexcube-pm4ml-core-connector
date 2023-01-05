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
        String statusCode ="5000";
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
                String customCBSMessage = null;
                String customCBSError= null;
                try {
                    customCBSMessage = "";
                    if (null != e.getResponseBody() && !e.getResponseBody().isEmpty()) {
                        if (DataFormatUtils.isJSONValid(e.getResponseBody())) {
                            JSONObject respObject = new JSONObject(e.getResponseBody());
                            if (respObject.has("message")) {
                                customCBSMessage = respObject.getString("message");
                            }
                            else if (respObject.has("error")){
                                customCBSMessage = respObject.getString("error");
                            }
                            else{
                                customCBSMessage="unknown CBS error";
                            }
                            if (respObject.has("error")) {
                                customCBSError = respObject.getString("error");
                            }
                        } else {
                            customCBSMessage = e.getResponseBody();
                        }
                    }
                    if ( customCBSError.contains("MIS-CUSCL02"))  {
                        //statusCode = String.valueOf(ErrorCode.GENERIC_ID_NOT_FOUND.getStatusCode());
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.GENERIC_PAYEE_REJECTION, customCBSMessage));
                        //  errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.GENERIC_ID_NOT_FOUND,StringUtils.parseJsonString(respObject.getString("message")));
                    }

                    else if (customCBSError.contains("CL-PMTV39") || customCBSError.contains("CL-PMTV32") || customCBSError.contains("CL-PMTV05"))
                    {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.PAYEE_LIMIT_ERROR, customCBSMessage));
                    }


                    else if (customCBSError.contains("CL-INVBR") || customCBSError.contains("CL-PMTV02") )
                    {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.GENERIC_ID_NOT_FOUND, customCBSMessage));
                    }
                   else if ( customCBSError.contains("MIS-CUSSRT01"))  {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.DESTINATION_COMMUNICATION_ERROR, customCBSMessage));
                    }
                    else {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, customCBSMessage.isEmpty() ? "Cannot made loan repayment process" : customCBSMessage));
                    }

                    statusCode = String.valueOf(errorResponse.getJSONObject("errorInformation").getInt("statusCode"));
                    errorDescription = errorResponse.getJSONObject("errorInformation").getString("description");

                } finally {
                    reasonText = "{ \"statusCode\": \"" + statusCode + "\"," +
                            "\"message\": \"" + customCBSMessage + "\"} ";
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
                    } else if (exception instanceof Exception || exception instanceof BeanValidationException) {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "CC logical transformation error"));
                    } else {
                        errorResponse = new JSONObject(ErrorCode.getErrorResponse(ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYEE));
                    }
                } finally {
                    httpResponseCode = errorResponse.getInt("errorCode");
                    errorResponse = errorResponse.getJSONObject("errorInformation");
                    statusCode = String.valueOf(errorResponse.getInt("statusCode"));
                    errorDescription = errorResponse.getString("description");
                    reasonText = "{ \"statusCode\": \"" + statusCode + "\"," +
                            "\"message\": \"" + errorDescription + "\"} ";
                    if (httpResponseCode == 504) {httpResponseCode = 500;}
                }
            }
                customJsonMessage.logJsonMessage("error", reasonText,
                    "Processing the exception at CustomErrorProcessor", null, null,
                    exception.getMessage());
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpResponseCode);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(reasonText);

        customJsonMessage.logJsonMessage("error", String.valueOf(exchange.getIn().getHeader("X-CorrelationId")),null,null,null,"Http Response Code" + httpResponseCode);
        customJsonMessage.logJsonMessage("error", String.valueOf(exchange.getIn().getHeader("X-CorrelationId")),null,null,null,"Response Body Message is " + exchange.getMessage().getBody(String.class));
    }
}