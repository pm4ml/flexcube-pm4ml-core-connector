package com.modusbox.client.router;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class AuthRouter extends RouteBuilder {

    private final String PATH_NAME = "Finflux Fetch Access Token API";
    private final String PATH = "/oauth/token";

    public void configure() {

        new ExceptionHandlingRouter(this);

        from("direct:getAuthHeader")
                .setProperty("downstreamRequestBody", simple("${body}"))
                .setProperty("clientId", simple("{{cbs.clientId}}"))
                .setProperty("grantType", simple("{{cbs.grantType}}"))
                .setProperty("scope", simple("{{cbs.scope}}"))
                .setProperty("username", simple("{{cbs.username}}"))
                .setProperty("password", simple("{{cbs.password}}"))
                .setProperty("isPasswordEncrypted", simple("{{cbs.isPasswordEncrypted}}"))
                .removeHeaders("Camel*")
                .setHeader("Fineract-Platform-TenantId", simple("{{cbs.tenantId}}"))
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
                        "'Request to POST {{cbs.host}}" + PATH + ", IN Payload: ${body}')")
                .to("{{cbs.host}}" + PATH)
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Called " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Response from POST {{cbs.host}}" + PATH + ", OUT Payload: ${body}')")
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
