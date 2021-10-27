package com.modusbox.client.router;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.validator.GetSettlementAmountCheckValidator;
import com.modusbox.client.validator.IdSubValueChecker;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class TransfersRouter extends RouteBuilder {


    private static final String TIMER_NAME_POST = "histogram_post_transfers_timer";
    private static final String TIMER_NAME_PUT = "histogram_put_transfers_timer";

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

    private final String Post_Repayment_PATH = "/loan";
    private final String Check_Settlement_Amount_PATH = "/balance?ACCOUNT_NUMBER=";

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();
    private final GetSettlementAmountCheckValidator settlementAmountCheckvalidator = new GetSettlementAmountCheckValidator();

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
                .setProperty("mfiSetlledGL",simple("${body.content.get('mfiSetlledGL')}"))


                .setBody(simple("${body.content}"))
                .marshal().json()
                .log("postTransfersRequest : ${body}")
                // Checked the settlement amount for over paid before repayment process
                .to("direct:checkSettlementAmount")

                // Validation for GetSettlementAmountCheckValidator

                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postTransfersRepaymentRequest.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()
                .unmarshal().json()

                // Do repayment process if above step is ok.
                .marshal().json()
                .to("direct:postLoanRepayment")

                // Error handling case after doing post transfer

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
                .doCatch(CCCustomException.class)
                .to("direct:extractCustomErrors")
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME_POST)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;


        from("direct:checkSettlementAmount")
                .to("direct:getAuthHeader")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{dfsp.host}}"+ Check_Settlement_Amount_PATH +"${exchangeProperty.mfiLoanAccountNo}&SETTLED_AMOUNT=${exchangeProperty.transactionAmount}")
                .unmarshal().json()
                .to("bean:customJsonMessage?method=logJsonMessage('info',  " +
                        "'Response from Flexcube Loan API with AccountId, ${body}', " +
                        "'Tracking the settlement amount response', 'Verify the response', null)")
        ;

        from("direct:postLoanRepayment")
                .to("direct:getAuthHeader")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .log("Request body : ${body}")
                .toD("{{dfsp.host}}"+ Post_Repayment_PATH)
                .unmarshal().json()

                .to("bean:customJsonMessage?method=logJsonMessage('info', " +
                        "'Response from Flexcube Loan API with AccountId, ${body}', " +
                        "'Tracking the clientInfo response', 'Verify the response', null)")

                .marshal().json()
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

    }
}
