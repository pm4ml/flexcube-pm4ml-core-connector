package com.modusbox.client.router;

import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class AuthRouter extends RouteBuilder {

    private final String PATH_NAME = "Finflux Fetch Access Token API";
    private final String PATH = "/oauth/token";

    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("direct:getAuthHeader")
                .setProperty("downstreamRequestBody", simple("${body}"))
                .setProperty("username", simple("{{dfsp.username}}"))
                .setProperty("password", simple("{{dfsp.password}}"))
                .setProperty("scope", simple("{{dfsp.scope}}"))
                .setProperty("clientId", simple("{{dfsp.client-id}}"))
                .setProperty("grantType", simple("{{dfsp.grant-type}}"))
                .setProperty("isPasswordEncrypted", simple("{{dfsp.is-password-encrypted}}"))
                .removeHeaders("Camel*")
                .setHeader("Fineract-Platform-TenantId", simple("{{dfsp.tenant-id}}"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setBody(constant(null))
                .bean("postAuthTokenRequest")
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Calling the " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Request to POST {{dfsp.host}}" + PATH + ", IN Payload: ${body}')")
                .to("{{dfsp.host}}" + PATH)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")
                .unmarshal().json(JsonLibrary.Gson)
                .setHeader("Authorization", simple("Bearer ${body['access_token']}"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Auth Token caught from " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Authorization: ${header.Authorization}')")
                .removeHeaders("CamelHttp*")
                .removeHeaders("Fineract-Platform-TenantId")
                .setBody(simple("${exchangeProperty.downstreamRequestBody}"))
        ;
    }
}
