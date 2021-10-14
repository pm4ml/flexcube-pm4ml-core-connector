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

/*
        from("direct:getAuthHeader")
                .setProperty("downstreamRequestBody", simple("${body}"))
                .setProperty("username", simple("{{dfsp.username}}"))
                .setProperty("password", simple("{{dfsp.password}}"))
                //.setProperty("scope", simple("{{dfsp.scope}}"))
                //.setProperty("clientId", simple("{{dfsp.client-id}}"))
                //.setProperty("grantType", simple("{{dfsp.grant-type}}"))
                //.setProperty("isPasswordEncrypted", simple("{{dfsp.is-password-encrypted}}"))
                .removeHeaders("Camel*")
                //.setHeader("Fineract-Platform-TenantId", simple("{{dfsp.tenant-id}}"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setBody(constant(""))
                .marshal().json()
                .transform(datasonnet("resource:classpath:mappings/postAuthTokenRequest.ds"))
                .setBody(simple("${body.content}"))
                .marshal().json()
                //.bean("postAuthTokenRequest")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                       // "${header.X-CorrelationId}, " +
                        "'Calling the " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Request to POST {{dfsp.host}}" + PATH + ", IN Payload: ${body}')")
                .toD("{{dfsp.host}}" + PATH)
                .unmarshal().json()

                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        //"${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")
                //.unmarshal().json(JsonLibrary.Gson)
                .setHeader("Authorization", simple("Bearer ${body['access_token']}"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                       // "${header.X-CorrelationId}, " +
                        "'Auth Token caught from " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Authorization: ${header.Authorization}')")
                .removeHeaders("CamelHttp*")
                //.removeHeaders("Fineract-Platform-TenantId")
                .setBody(simple("${exchangeProperty.downstreamRequestBody}"))
        ;

        */
        from("direct:getAuthHeader")
                .log("Prepare Downstream Call")
                .setProperty("authHeader", simple("{{dfsp.username}}:{{dfsp.password}}"))
                .process(encodeAuthHeader)
                .log("Get AuthHeader")
                .removeHeaders("CamelHttp*")
                .setHeader("Content-Type", constant("application/json"));
                //.setHeader("X-Fineract-Platform-TenantId",constant("{{dfsp.tenant-id}}"))
                //.setHeader("X-api-key",constant("{{dfsp.api-key}}"));



    }
}
