package com.modusbox.client.router;

import com.modusbox.client.exception.CamelErrorProcessor;
import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import org.apache.camel.builder.RouteBuilder;

import javax.annotation.Generated;

/**
 * Generated from OpenApi specification by Camel REST DSL generator.
 */
@Generated("org.apache.camel.generator.openapi.PathGenerator")
public final class CoreConnectorAPI extends RouteBuilder {

    /**
     * Defines Apache Camel routes using REST DSL fluent API.
     */
    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        CamelErrorProcessor errorProcessor = new CamelErrorProcessor();
        //new ExceptionHandlingRouter(this);
        onException(Exception.class)
                //.logContinued(true).logRetryAttempted(true).logExhausted(true).logStackTrace(true)
                //.retryAttemptedLogLevel(LoggingLevel.INFO).retriesExhaustedLogLevel(LoggingLevel.INFO)
                //.maximumRedeliveries(3).redeliveryDelay(250).backOffMultiplier(2).useExponentialBackOff()

                .handled(true)
                .log("-- processing error")
                .process(errorProcessor)
                .log("-- error processing complete")
        ;

        from("cxfrs:bean:api-rs-server?bindingStyle=SimpleConsumer")
                .to("bean-validator://x")
                .toD("direct:${header.operationName}");

    }
}
