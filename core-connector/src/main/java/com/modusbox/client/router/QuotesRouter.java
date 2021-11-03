package com.modusbox.client.router;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.validator.SettlementAmountValidator;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class QuotesRouter extends RouteBuilder {

    private static final String TIMER_NAME = "histogram_post_quoterequests_timer";

    public static final Counter reqCounter = Counter.build()
            .name("counter_post_quoterequests_requests_total")
            .help("Total requests for POST /quoterequests.")
            .register();

    private static final Histogram reqLatency = Histogram.build()
            .name("histogram_post_quoterequests_request_latency")
            .help("Request latency in seconds for POST /quoterequests.")
            .register();

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();
    private final SettlementAmountValidator settlementAmountvalidator = new SettlementAmountValidator();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("direct:postQuoteRequests").routeId("com.modusbox.postQuoterequests").doTry()
                .process(exchange -> {
                    reqCounter.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("direct:getAuthHeader")
                .process(exchange -> System.out.println("Starting POST Quotes API called*****"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received POST /quoterequests', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')")
                /*
                 * BEGIN processing
                 */


                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postQuoterequestsResponse.ds"))
                .setProperty("transferAmount",simple("${body.content.get('transferAmount')}"))
                .setBody(simple("${body.content}"))
                .marshal().json()

                // Validation for repayment settled amount
                .process(settlementAmountvalidator)
                /*
                 * END processing
                 */

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Response for POST /quoterequests', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .process(exchange -> System.out.println("Ending POST Quotes API called*****"))
                .removeHeaders("*", "X-*")
                .doCatch(CCCustomException.class, java.lang.Exception.class)
                .to("direct:extractCustomErrors")
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;
    }
}
