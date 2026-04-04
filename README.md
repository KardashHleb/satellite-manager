
СЕРЕВЕР ЗАПУСКАЕТСЯ НА ПОРТЕ 8082 т.к. на локальной машине порт 8080 и 8081 были уже заняты: 

http://localhost:8082
http://localhost:8082/test.html

curl http://localhost:8082/api/overview

Основная информация указана в файле - application.properties 
server.port=8082
