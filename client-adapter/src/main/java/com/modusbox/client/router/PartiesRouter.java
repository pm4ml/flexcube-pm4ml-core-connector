package com.modusbox.client.router;

import com.modusbox.client.processor.BodyChecker;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.util.UUID;

public class PartiesRouter extends RouteBuilder {

    private final String PATH_NAME = "Finflux Advance Fetch Due API";
    private final String PATH = "/v1/paymentgateway/billerpayments/advance-fetch";

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:getParties")
                .routeId("com.modusbox.getParties")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the request', " +
                        "'Call the " + PATH_NAME + ",  Track the response', " +
                        "'Input Payload: ${body}')")
                .to("direct:getAuthHeader")
                //.setProperty("uuid", simple(UUID.randomUUID().toString()))
                .process(exchange -> exchange.setProperty("uuid", UUID.randomUUID().toString()))
                .removeHeaders("Camel*")
                .setHeader("Fineract-Platform-TenantId", constant("hanastaging"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setBody(constant(null))
                .bean("getPartiesRequest")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Calling the " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Request to POST {{cbs.host}}" + PATH + ", IN Payload: ${body} IN Headers: ${headers}')")
                .to("{{cbs.host}}" + PATH)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{cbs.host}}" + PATH + ", OUT Payload: ${body}')")
                .process(new BodyChecker())
                .bean("getPartiesResponse")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .removeHeaders("*", "X-*")
        ;
    }
}
