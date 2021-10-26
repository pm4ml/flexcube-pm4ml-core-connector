package com.modusbox.client.router;

import com.modusbox.client.customexception.CCCustomException;
import com.modusbox.client.customexception.CloseWrittenOffAccountException;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.processor.PadLoanAccount;
import com.modusbox.client.processor.SetPropertiesLoanInfo;
import com.modusbox.client.validator.AccountNumberFormatValidator;
import com.modusbox.client.validator.GetPartyResponseValidator;
import com.modusbox.client.validator.IdSubValueChecker;
import com.modusbox.client.validator.PhoneNumberValidation;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.util.UUID;

public class PartiesRouter extends RouteBuilder {

    private final PadLoanAccount padLoanAccount = new PadLoanAccount();
    private final SetPropertiesLoanInfo setPropertiesForLoanInfo = new SetPropertiesLoanInfo();
    private final PhoneNumberValidation phoneNumberValidation = new PhoneNumberValidation();
    private final AccountNumberFormatValidator accountNumberFormatValidator = new AccountNumberFormatValidator();
    private final GetPartyResponseValidator getPartyResponseValidator = new GetPartyResponseValidator();

    private final IdSubValueChecker idSubValueChecker = new IdSubValueChecker();

    private static final String TIMER_NAME = "histogram_get_parties_timer";

    public static final Counter reqCounter = Counter.build()
            .name("counter_get_parties_requests_total")
            .help("Total requests for GET /parties.")
            .register();

    private static final Histogram reqLatency = Histogram.build()
            .name("histogram_get_parties_request_latency")
            .help("Request latency in seconds for GET /parties.")
            .register();

    private final String PATH_NAME = "Flexcube Advance Fetch Due API";
    private final String PATH = "/loan";

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);

        from("direct:getPartiesByIdTypeIdValue").routeId("com.modusbox.getPartiesByIdTypeIdValue").doTry()
                .process(exchange -> {
                    reqCounter.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
                })

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the request', " +
                        "'Call the Mambu API,  Track the response', " +
                        "'Input Payload: ${body}')") // default logger

                .process(idSubValueChecker)

                .doCatch(CCCustomException.class)
                    .to("direct:extractCustomErrors")
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
            }).end()
        ;

       from("direct:getPartiesByIdTypeIdValueIdSubValue").routeId("com.modusbox.getParties").doTry()
                .process(exchange -> {
                    reqCounter.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME, reqLatency.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Request received GET /parties/${header.idType}/${header.idValue}', " +
                        "'Tracking the request', " +
                        "'Call the " + PATH_NAME + ",  Track the response', " +
                        "'Input Payload: ${body}')") // default logger

                .process(accountNumberFormatValidator)
                .process(padLoanAccount)
                .to("direct:getAuthHeader")
                .setHeader("MFIName", constant("{{dfsp.name}}"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Calling the " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Request to GET {{dfsp.host}}" + PATH + ", IN Payload: ${body} IN Headers: ${headers}')")

                .toD("{{dfsp.host}}" + PATH + "?ACCOUNT_NUMBER=${exchangeProperty.loanAccount}")
                .unmarshal().json()

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from GET {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")

                .marshal().json()
                .process(getPartyResponseValidator)
                .process(setPropertiesForLoanInfo)
                .process(phoneNumberValidation)
                .unmarshal().json()

                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/getPartiesResponse.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()
                .unmarshal().json()
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "'Response for GET /parties/${header.idType}/${header.idValue}/${header.idSubValue} API', " +
                        "'Tracking the response', " +
                        "null, " +
                        "'Output Payload: ${body}')") // default logger
                .removeHeaders("*", "X-*")
                .doCatch(CCCustomException.class,CloseWrittenOffAccountException.class)
                    .to("direct:extractCustomErrors")

                .doFinally().process(exchange -> {
            ((Histogram.Timer) exchange.getProperty(TIMER_NAME)).observeDuration(); // stop Prometheus Histogram metric
        }).end()
        ;




    }
}