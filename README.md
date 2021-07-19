# mojaloop-finflux-core-connector
Project for Mojaloop connector to Finflux core banking system.

Most of the content details and instructions about development and deployment can be found into the main [template project](https://github.com/pm4ml/pm4ml-core-connector-rest-template). 
Additional or different topics specified below.

### Overwrite application properties

To run the Finflux Core Connector and specify the proper credentials for CBS API connection:
```
java \
-Dml-conn.outbound.host="http://localhost:4001" \
-Dcbs.host="https://cbs/api" \
-Dcbs.username="user" \
-Dcbs.password="pass" \
-Dcbs.scope="scope" \
-Dcbs.client-id="id" \
-Dcbs.client-secret="secret" \
-Dcbs.grant-type="type" \
-Dcbs.is-password-encrypted="false" \
-Dcbs.tenant-id="id" \
-jar ./client-adapter/target/client-adapter.jar
```
```
docker run --rm \
-e MLCONN_OUTBOUND_ENDPOINT="http://localhost:4001" \
-e CBS_HOST="https://cbs/api" \
-e CBS_USERNAME="user" \
-e CBS_PASSWORD="P\@ss0rd" \
-e CBS_AUTH_CLIENT_ID="id" \
-e CBS_AUTH_CLIENT_SECRET="secret" \
-e CBS_AUTH_GRANT_TYPE="type" \
-e CBS_AUTH_SCOPE="scope" \
-e CBS_AUTH_ENCRYPTED_PASS="false" \
-e CBS_AUTH_TENANT_ID="id" \
-p 3003:3003 finflux-cc:latest
```
**NOTE:** keep the values in double quotes (") and scape any special character (\\@).