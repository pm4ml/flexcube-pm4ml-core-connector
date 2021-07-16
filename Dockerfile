### Build runtime image
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=client-adapter/target/*.jar
COPY ${JAR_FILE} app.jar
ENV MLCONN_OUTBOUND_ENDPOINT=http://simulator:3004
ENV CBS_HOST=https://localhost/api
ENV CBS_USERNAME=username
ENV CBS_PASSWORD=password
ENV CBS_AUTH_CLIENT_ID=clientId
ENV CBS_AUTH_CLIENT_SECRET=clientSecret
ENV CBS_AUTH_GRANT_TYPE=grantType
ENV CBS_AUTH_SCOPE=scope
ENV CBS_AUTH_ENCRYPTED_PASS=false
ENV CBS_AUTH_TENANT_ID=tenantId
ENTRYPOINT ["java", "-DmlConnector.outbound.host=${MLCONN_OUTBOUND_ENDPOINT}", "-Dcbs.host=${CBS_HOST}", "-Dcbs.username=${CBS_USERNAME}", "-Dcbs.password=${CBS_PASSWORD}", "-Dcbs.clientId=${CBS_AUTH_CLIENT_ID}", "-Dcbs.clientSecret=${CBS_AUTH_CLIENT_SECRET}", "-Dcbs.grantType=${CBS_AUTH_GRANT_TYPE}", "-Dcbs.scope=${CBS_AUTH_SCOPE}", "-Dcbs.isPasswordEncrypted=${CBS_AUTH_ENCRYPTED_PASS}", "-Dcbs.tenantId=${CBS_AUTH_TENANT_ID}", "-jar", "/app.jar"]
EXPOSE 3003