package com.modusbox.client.router;

import com.modusbox.client.processor.ErrorHandler;
import org.apache.camel.builder.RouteBuilder;

public class ExceptionHandlingRouter {

    private ErrorHandler errorProcessor = new ErrorHandler();

    public ExceptionHandlingRouter(RouteBuilder routeBuilder) {
        System.out.println("Entered error handling");
        routeBuilder
                .onException(Exception.class)
                .handled(true)
                .log("-- processing error")
                .process(this.errorProcessor)
                .log("-- error processing complete");
    }
}
