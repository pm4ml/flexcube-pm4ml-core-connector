package com.modusbox.client.router;

import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.processor.TokenStore;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class AuthRouter extends RouteBuilder {
    private final String PATH_NAME = "Flexcube Fetch Access Token API";
    private final String PATH = "/authenticate/generateToken";
    private final String PATH2 = "/authenticate/generateNewToken";
    private final RouteExceptionHandlingConfigurer exceptionHandlingConfigurer = new RouteExceptionHandlingConfigurer();

    public void configure() {

        exceptionHandlingConfigurer.configureExceptionHandling(this);
        //new ExceptionHandlingRouter(this);

        from("direct:getAuthHeader")
                .setProperty("downstreamRequestBody", simple("${body}"))
                .setProperty("AccessToken", method(TokenStore.class, "getAccessToken()"))
                .choice()
                .when(method(TokenStore.class, "getRefreshToken()").isEqualTo(""))
                    .setProperty("username", simple("{{dfsp.username}}"))
                    .setProperty("password", simple("{{dfsp.password}}"))
                    .removeHeaders("Camel*")
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
                            "${header.X-CorrelationId}, " +
                            "'Calling the access token " + PATH_NAME + "', " +
                            "null, " +
                            "null, " +
                            "'Request to POST {{dfsp.host}}" + PATH +", IN Payload: ${body}')")
                    .toD("{{dfsp.host}}" + PATH)
                    .unmarshal().json()
                    .setProperty("RefreshToken", simple("${body['refreshToken']}"))
                    .setProperty("RefreshTokenExpiration", simple("${body['expireIn']}"))
                    .bean(TokenStore.class, "setRefreshToken(${exchangeProperty.RefreshToken}, ${exchangeProperty.RefreshTokenExpiration})")
                    //.bean(TokenStore.class, "setAccessToken(${body['accessToken']}, ${body['expireIn']})")
                    .marshal().json()
                    .transform(datasonnet("resource:classpath:mappings/postAuthRefreshTokenRequest.ds"))
                    .setBody(simple("${body.content}"))
                    .marshal().json()
                    .to("bean:customJsonMessage?method=logJsonMessage(" +
                            "'info', " +
                            "${header.X-CorrelationId}, " +
                            "'Calling the refresh token " + PATH_NAME + "', " +
                            "null, " +
                            "null, " +
                            "'Request to POST {{dfsp.host}}" + PATH +", IN Payload: ${body}')")
                    .toD("{{dfsp.host}}" + PATH2)
                    .unmarshal().json()
                    .setProperty("AccessToken", simple("${body['accessToken']}"))
                    .setProperty("AccessTokenExpiration", simple("${body['expireIn']}"))
                    .bean(TokenStore.class, "setAccessToken(${exchangeProperty.AccessToken}, ${exchangeProperty.AccessTokenExpiration})")

                    .to("bean:customJsonMessage?method=logJsonMessage(" +
                            "'info', " +
                            "${header.X-CorrelationId}, " +
                            "'Called refresh token " + PATH_NAME + "', " +
                            "null, " +
                            "null, " +
                            "'Response from POST {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")

                .when(method(TokenStore.class, "getAccessToken()").isEqualTo(""))
                    .setProperty("RefreshToken",method(TokenStore.class, "getRefreshToken()"))
                    .transform(datasonnet("resource:classpath:mappings/postAuthRefreshTokenRequest.ds"))
                    .setBody(simple("${body.content}"))
                    .marshal().json()
                    .to("bean:customJsonMessage?method=logJsonMessage(" +
                            "'info', " +
                            "${header.X-CorrelationId}, " +
                            "'Calling the refresh token " + PATH_NAME + "', " +
                            "null, " +
                            "null, " +
                            "'Request to POST {{dfsp.host}}" + PATH +", IN Payload: ${body}')")
                    .toD("{{dfsp.host}}" + PATH2)
                    .unmarshal().json()
                    .setProperty("AccessToken", simple("${body['accessToken']}"))
                    .setProperty("AccessTokenExpiration", simple("${body['expireIn']}"))
                    .bean(TokenStore.class, "setAccessToken(${exchangeProperty.AccessToken}, ${exchangeProperty.AccessTokenExpiration})")

                    .to("bean:customJsonMessage?method=logJsonMessage(" +
                            "'info', " +
                            "${header.X-CorrelationId}, " +
                            "'Called refresh token " + PATH_NAME + "', " +
                            "null, " +
                            "null, " +
                            "'Response from POST {{dfsp.host}}" + PATH + ", OUT Payload: ${body}')")
                .otherwise()
                    .setProperty("AccessToken", method(TokenStore.class, "getAccessToken()"))
                .end()

                .setHeader("Authorization", simple("Bearer ${exchangeProperty.AccessToken}"))
                .to("bean:customJsonMessage?method=logJsonMessage(" +
                        "'info', " +
                        "${header.X-CorrelationId}, " +
                        "'Auth Token caught from " + PATH_NAME + "', " +
                        "null, " +
                        "null, " +
                        "'Authorization: ${header.Authorization}')")
                .removeHeaders("CamelHttp*")
                .setBody(simple("${exchangeProperty.downstreamRequestBody}"))
        ;

    }
}
