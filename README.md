# flexcube-pm4ml-core-connector

Most of the content details and instructions about development and deployment can be found into 
the main [template project](https://github.com/pm4ml/template-rest-pm4ml-core-connector). 
Additional or different topics specified below.

### Overwrite application properties

To run application and specify the proper credentials for DFSP API connection
(it is not required specify all the fields if it isn't used):
```
java \
-Dml-conn.outbound.host="http://localhost:4001" \
-Ddfsp.host="https://dfsp/api" \
-Ddfsp.username="user" \
-Ddfsp.password="pass" \
-Ddfsp.scope="scope" \
-Ddfsp.client-id="id" \
-Ddfsp.client-secret="secret" \
-Ddfsp.grant-type="type" \
-Ddfsp.is-password-encrypted="false" \
-Ddfsp.tenant-id="id" \
-jar ./core-connector/target/core-connector.jar
```
```
docker run --rm \
-e MLCONN_OUTBOUND_ENDPOINT="http://localhost:4001" \
-e DFSP_HOST="https://dfsp/api" \
-e DFSP_USERNAME="user" \
-e DFSP_PASSWORD="P\@ss0rd" \
-e DFSP_AUTH_CLIENT_ID="id" \
-e DFSP_AUTH_CLIENT_SECRET="secret" \
-e DFSP_AUTH_GRANT_TYPE="type" \
-e DFSP_AUTH_SCOPE="scope" \
-e DFSP_AUTH_ENCRYPTED_PASS="false" \
-e DFSP_AUTH_TENANT_ID="id" \
-p 3003:3003 core-connector:latest
```
**NOTE:** keep the values in double quotes (") and scape any special character (\\@).