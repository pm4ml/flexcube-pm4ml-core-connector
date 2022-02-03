package com.modusbox.client.router;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.validator.PostTransferResponseValidator;
import com.modusbox.client.validator.SettledBalanceValidator;
import com.modusbox.client.validator.PostTransferRequestValidator;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class TransfersRouter extends RouteBuilder {


    private static final String TIMER_NAME_POST = "histogram_post_transfers_timer";
    private static final String TIMER_NAME_PUT = "histogram_put_transfers_timer";
    private static final String TIMER_NAME_GET = "histogram_get_transfers_timer";

    public static final Counter reqCounterPost = Counter.build()
            .name("counter_post_transfers_requests_total")
            .help("Total requests for POST /transfers.")
            .register();

    public static final Counter reqCounterPut = Counter.build()
            .name("counter_put_transfers_requests_total")
            .help("Total requests for POST /transfers.")
            .register();

    private static final Histogram reqLatencyPost = Histogram.build()
            .name("histogram_post_transfers_request_latency")
            .help("Request latency in seconds for POST /transfers.")
            .register();

    private static final Histogram reqLatencyPut = Histogram.build()
            .name("histogram_put_transfers_request_latency")
            .help("Request latency in seconds for PUT /transfers.")
            .register();

    public static final Counter reqCounterGet = Counter.build()
            .name("counter_get_transfers_requests_total")
            .help("Total requests for GET /transfers.")
            .register();

    private static final Histogram reqLatencyGet = Histogram.build()
            .name("histogram_get_transfers_request_latency")
            .help("Request latency in seconds for GET /transfers.")
            .register();

    private final String Post_Repayment_PATH = "/loan";
    private final SettledBalanceValidator settledBalanceValidator = new SettledBalanceValidator();
    private final PostTransferRequestValidator postTransferRequestValidator = new PostTransferRequestValidator();
    private final PostTransferResponseValidator postTransferResponseValidator = new PostTransferResponseValidator();
    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);

        from("direct:postTransfers").routeId("com.modusbox.postTransfers").doTry()
                .process(exchange -> {
                    reqCounterPost.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME_POST, reqLatencyPost.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received POST /transfers', " +
                        "'Tracking the request', " +
                        "'Track the response', " +
                        "'Input Payload: ${body}')") // default logger
                /*
                 * BEGIN processing
                 */
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postTransfersRequest.ds"))
                .setProperty("mfiLoanAccountNo",simple("${body.content.get('mfiLoanAccountNo')}"))
                .setProperty("transactionAmount",simple("${body.content.get('transactionAmount')}"))
                .setProperty("transactionId",simple("${body.content.get('transactionId')}"))
                .setProperty("transferID",simple("${body.content.get('transferID')}"))
                .setProperty("makerUserID",simple("{{dfsp.username}}"))
                .setProperty("mfiOfficeName",simple("${body.content.get('mfiOfficeName')}"))
                .setProperty("walletFspId",simple("${body.content.get('walletFspId')}"))
                .setProperty("mfiSetlledGL",constant("{{dfsp.settledGL}}"))

                .setBody(simple("${body.content}"))
                .marshal().json()
                .log("postTransfersRequest : ${body}")

                .process(settledBalanceValidator)

                .transform(datasonnet("resource:classpath:mappings/postTransfersRepaymentRequest.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()

                // Validation the required fields and value before post repayment process.
                .process(postTransferRequestValidator)
                // Do repayment process if above step is ok.

                .to("direct:postLoanRepayment")

                /*
                 * END processing
                 */

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Response for POST /transfers', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')") // default logger

                .removeHeaders("*", "X-*")
                .doCatch(CCCustomException.class,java.lang.Exception.class)
                .to("direct:extractCustomErrors")
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME_POST)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;

        from("direct:postLoanRepayment")
                .to("direct:getAuthHeader")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .log("Request body : ${body}")
                .toD("{{dfsp.host}}"+ Post_Repayment_PATH)
                .unmarshal().json()
                .marshal().json()
                // Error handling case after doing post transfer
                .process(postTransferResponseValidator)

                .to("bean:customJsonMessage?method=logJsonMessage('info', " +
                        "'Response from Flexcube Loan API with AccountId, ${body}', " +
                        "'Tracking the clientInfo response', 'Verify the response', null)")

                //.marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postTransfersResponse.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()
                .unmarshal().json()
        ;


        from("direct:putTransfersByTransferId").routeId("com.modusbox.putTransfers").doTry()
                .process(exchange -> {
                    reqCounterPut.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME_PUT, reqLatencyPut.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received PUT /transfers', " +
                        "'Tracking the request', " +
                        "'Call the PUT /transfers,  Track the response', " +
                        "'Input Payload: ${body}')") // default logger

                .removeHeaders("*", "X-*")

                .doCatch(CCCustomException.class)
                .to("direct:extractCustomErrors")

                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME_PUT)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;

        from("direct:getTransfersByTransferId").routeId("com.modusbox.getTransfers").doTry()
                .process(exchange -> {
                    reqCounterGet.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME_GET, reqLatencyGet.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Request received, GET /transfers/${header.transferId}', " +
                        "null, null, null)")
                /*
                 * BEGIN processing
                 */

                .removeHeaders("CamelHttp*")
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Calling Hub API, get transfers, GET {{ml-conn.outbound.host}}', " +
                        "'Tracking the request', 'Track the response', 'Input Payload: ${body}')")
                .toD("{{ml-conn.outbound.host}}/transfers/${header.transferId}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .unmarshal().json()
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Response from Hub API, get transfers: ${body}', " +
                        "'Tracking the response', 'Verify the response', null)")
//                .process(exchange -> System.out.println())

                .choice()
                .when(simple("${body['statusCode']} != null"))
//                .process(exchange -> System.out.println())
                    .to("direct:catchMojaloopError")
                .endDoTry()
           
//                .process(exchange -> System.out.println())
            
                .choice()
                .when(simple("${body['fulfil']} != null"))
//                .process(exchange -> System.out.println())            
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/getTransfersResponse.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()
                .endDoTry()
            
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/getTransfersResponse.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()

                /*
                 * END processing
                 */
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Final Response: ${body}', " +
                        "null, null, 'Response of GET /transfers/${header.transferId} API')")

                .doCatch(CCCustomException.class, HttpOperationFailedException.class, JSONException.class)
                    .to("direct:extractCustomErrors")            
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME_GET)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;
    }
}
