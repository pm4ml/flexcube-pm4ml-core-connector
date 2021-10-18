package com.modusbox.client.router;

import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.processor.EncodeAuthHeader;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class AuthRouter extends RouteBuilder {

//    private final String PATH_NAME = "Flexcube Fetch Access Token API";
//    private final String PATH = "/oauth/token";

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();
    private final EncodeAuthHeader encodeAuthHeader = new EncodeAuthHeader();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("direct:getAuthHeader")
                .log("Prepare Downstream Call")
                //.setProperty("authHeader", simple("{{dfsp.username}}:{{dfsp.password}}"))
                //.setProperty("username", simple("{{dfsp.username}}"))
                //.setProperty("password", simple("{{dfsp.password}}"))
                //.process(encodeAuthHeader)
                .log("Get AuthHeader")
                .removeHeaders("CamelHttp*")
                .setHeader("Content-Type", constant("application/json"));

    }
}
