package com.modusbox.client.router;

import com.modusbox.client.processor.BodyChecker;
import com.modusbox.client.processor.PadLoanAccount;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.util.UUID;

public class PartiesRouter extends RouteBuilder {

    private final PadLoanAccount padLoanAccount = new PadLoanAccount();
    private static final String TIMER_NAME = "histogram_get_parties_timer";

    public static final Counter reqCounter = Counter.build()
            .name("counter_get_parties_requests_total")
            .help("Total requests for GET /parties.")
            .register();

    private static final Histogram reqLatency = Histogram.build()
            .name("histogram_get_parties_request_latency")
            .help("Request latency in seconds for GET /parties.")
            .register();

    private final String PATH_NAME = "Finflux Advance Fetch Due API";
    private final String PATH = "/v1/paymentgateway/billerpayments/advance-fetch";

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:getParties").routeId("com.modusbox.getParties").doTry()
                .process(exchange -> {
                    reqCounter.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Request received GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the request', " +
                        "'Call the " + PATH_NAME + ",  Track the response', " +
                        "'Input Payload: ${body}')") // default logger
                /*
                 * BEGIN processing
                 */
                .process(padLoanAccount)
                .to("direct:getAuthHeader")
                .process(exchange -> exchange.setProperty("uuid", UUID.randomUUID().toString()))
                .removeHeaders("Camel*")
                .setHeader("Fineract-Platform-TenantId", constant("{{dfsp.tenant-id}}"))
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
                        "'Request to POST {{dfsp.host}}" + PATH + ", IN Payload: ${body} IN Headers: ${headers}')")
                .to("{{dfsp.host}}" + PATH)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")
                .process(new BodyChecker())
                .setProperty("mfiName", constant("{{dfsp.name}}"))
                .bean("getPartiesResponse")
                /*
                 * END processing
                 */
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Response for GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')") // default logger
                .removeHeaders("*", "X-*")
                .doFinally().process(exchange -> {
            ((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
        }).end()
        ;
    }
}