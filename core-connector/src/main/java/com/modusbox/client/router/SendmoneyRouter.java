package com.modusbox.client.router;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class SendmoneyRouter extends RouteBuilder {

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:postSendmoney")
                .routeId("com.modusbox.postSendmoney")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received POST /sendmoney', " +
                        "'Tracking the request', " +
                        "'Call the Mojaloop Connector Outbound API, Track the response', " +
                        "'Input Payload: ${body}')")
                .setProperty("origPayload", simple("${body}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Calling the Mojaloop Connector Outbound API, " +
                        "POST {{ml-conn.outbound.host}}', " +
                        "null, " +
                        "null, " +
                        "'Request to POST {{ml-conn.outbound.host}}/transfers, IN Payload: ${body} IN Headers: ${headers}')")
                //.marshal().json(JsonLibrary.Gson)
                //.toD("{{ml-conn.outbound.host}}/transfers?bridgeEndpoint=true&throwExceptionOnFailure=false")
                //.unmarshal().json(JsonLibrary.Gson)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called the Mojaloop Connector', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{ml-conn.outbound.host}}/transfers, OUT Payload: ${body}')")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for POST /sendmoney', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .removeHeaders("*", "X-*")
                .setBody(simple(""))
        ;

        from("direct:putSendmoneyById")
                .routeId("com.modusbox.putSendmoneyById")
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Request received, PUT /sendmoney/${header.transferId}', " +
                        "null, null, 'Input Payload: ${body}')")
                .setProperty("origPayload", simple("${body}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Calling Mojaloop Connector Outbound API', " +
                        "'Tracking the request', 'Track the response', " +
                        "'Request sent to PUT {{ml-conn.outbound.host}}/transfers/${header.transferId}')")
                //.marshal().json(JsonLibrary.Gson)
                //.toD("{{ml-conn.outbound.host}}/transfers/${header.transferId}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                //.unmarshal().json(JsonLibrary.Gson)
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Response from Mojaloop Connector Outbound API: ${body}', " +
                        "'Tracking the response', 'Verify the response', null)")
                .setBody(simple(""))
        ;
    }
}
