The server starts on port 8082 because ports 8080 and 8081 were already taken on the local machine:

http://localhost:8082

http://localhost:8082/test.html

curl http://localhost:8082/api/overview

The main settings are in the file application.properties
server.port=8082
