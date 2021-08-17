package com.modusbox.client.router;

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

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("cxfrs:bean:api-rs-server?bindingStyle=SimpleConsumer")
                .to("bean-validator://x")
                .toD("direct:${header.operationName}");

    }
}
