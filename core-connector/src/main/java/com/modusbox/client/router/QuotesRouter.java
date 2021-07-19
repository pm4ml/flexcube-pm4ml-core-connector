package com.modusbox.client.router;

import org.apache.camel.builder.RouteBuilder;

public class QuotesRouter extends RouteBuilder {

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:postQuoterequests")
                .routeId("com.modusbox.postQuoterequests")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received POST /quoterequests', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')")
                .bean("postQuoterequestsResponseMock")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for POST /quoterequests', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .removeHeaders("*", "X-*")
        ;
    }
}
