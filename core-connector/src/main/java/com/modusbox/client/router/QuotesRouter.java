package com.modusbox.client.router;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.validator.SettlementAmountValidator;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONException;

import java.net.SocketTimeoutException;

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

    private final String Check_Settlement_Amount_PATH = "/loan/getBalance/";

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("direct:postQuoteRequests").routeId("com.modusbox.postQuoterequests").doTry()
                .process(exchange -> {
                    reqCounter.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
                })
                .log("Starting POST Quotes API called*****")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received POST /quoterequests', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')")
                /*
                 * BEGIN processing
                 */

                .setProperty("postQuoteRequest",simple("${body}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/getPaymentAmountValidationRequest.ds"))
                .setProperty("accountId", simple("${body.content.get('accountId')}"))
                .setProperty("transferAmount",simple("${body.content.get('transferAmount')}"))
                .setBody(simple("${body.content}"))
                .marshal().json()

                // Validation for repayment settled amount
                .process(settlementAmountvalidator)
                /*
                 * END processing
                 */
                .to("direct:checkSettlementAmount")
                .log("${body}")
                .setBody(simple("${exchangeProperty.postQuoteRequest}"))
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postQuoterequestsResponse.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Response for POST /quoterequests', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')")
                .log("Ending POST Quotes API called*****")
                .removeHeaders("*", "X-*")
                .doCatch(CCCustomException.class, java.lang.Exception.class, HttpOperationFailedException.class, JSONException.class, ConnectTimeoutException.class, SocketTimeoutException.class, HttpHostConnectException.class)
                    .to("direct:extractCustomErrors")
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;

        from("direct:checkSettlementAmount")
                .to("direct:getAuthHeader")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Calling the Amount check validation API GET {{dfsp.host}}"+ Check_Settlement_Amount_PATH +"${exchangeProperty.accountId}/${exchangeProperty.transferAmount}', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')")
                .toD("{{dfsp.host}}"+ Check_Settlement_Amount_PATH +"${exchangeProperty.accountId}/${exchangeProperty.transferAmount}")
                .unmarshal().json()
                .to("bean:customJsonMessage?method=logJsonMessage('info',  " +
                        "'Response from Flexcube Amount check validation API with AccountId and Amount, ${body}', " +
                        "'Tracking the settlement amount response', 'Verify the response', null)")
        ;
    }
}
