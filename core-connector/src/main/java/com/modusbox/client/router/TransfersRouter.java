package com.modusbox.client.router;

import com.modusbox.client.processor.BodyChecker;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class TransfersRouter extends RouteBuilder {

    //private final String PATH_NAME_POST = "Finflux hardcoded postTransfer response";
    //private final String PATH = "/v1/paymentgateway/billerpayments/advance-fetch";

    private final String PATH_NAME_PUT = "Finflux Bill Payment Direct Process API";
    private final String PATH = "/v1/paymentgateway/billerpayments/process-direct?paymentType=Mojaloop";

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:postTransfers")
                .routeId("com.modusbox.postTransfers")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received POST /transfers', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')")
                .marshal().json(JsonLibrary.Gson)
                .bean("postTransfersResponse")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for POST /transfers', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .removeHeaders("*", "X-*")
                //.setBody(constant(null))
        ;

        from("direct:putTransfers")
                .routeId("com.modusbox.putTransfers")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received PUT /transfers', " +
                        "'Tracking the request', " +
                        "'Call the " + PATH_NAME_PUT + ",  Track the response', " +
                        "'Input Payload: ${body}')")
                .to("direct:getAuthHeader")
                .removeHeaders("Camel*")
                .setHeader("Fineract-Platform-TenantId", constant("hanastaging"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .bean("putTransfersRequest")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Calling the " + PATH_NAME_PUT + "', " +
                        "null, " +
                        "null, " +
                        "'Request to POST {{cbs.host}}" + PATH + ", IN Payload: ${body} IN Headers: ${headers}')")
                .to("{{cbs.host}}" + PATH)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME_PUT + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{cbs.host}}" + PATH + ", OUT Payload: ${body}')")
                .process(new BodyChecker())
                //.bean("putTransfersResponse")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for PUT /transfers', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: empty')")
                .removeHeaders("*", "X-*")
                .setBody(constant(null))
        ;

    }
}
