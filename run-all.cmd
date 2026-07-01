@echo off
echo ===================================================
echo  Unilabs Notification Center - Arranque
echo ===================================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
set PATH=%JAVA_HOME%\bin;C:\maven\apache-maven-3.9.16\bin;%PATH%

echo.
echo [1/3] A verificar infraestrutura Docker...
docker compose up -d
echo.

echo [2/3] A instalar modulo shared...
call mvn -pl shared install -q
echo.

echo [3/3] A iniciar Backend (porta 8080)...
start "Backend - Porta 8080" cmd /k "cd /d %~dp0 && set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot && set PATH=%%JAVA_HOME%%\bin;C:\maven\apache-maven-3.9.16\bin;%%PATH%% && mvn -pl backend spring-boot:run"

echo A aguardar arranque do backend (30 segundos)...
timeout /t 30 /nobreak > nul

echo A iniciar Portal Vaadin (porta 8082)...
start "Portal - Porta 8082" cmd /k "cd /d %~dp0 && set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot && set PATH=%%JAVA_HOME%%\bin;C:\maven\apache-maven-3.9.16\bin;%%PATH%% && mvn -pl portal compile spring-boot:run"

echo.
echo ===================================================
echo  Servicos disponiveis:
echo  Backend / Swagger : http://localhost:8080/swagger-ui.html
echo  Portal Vaadin     : http://localhost:8082
echo  RabbitMQ UI       : http://localhost:15672  (guest/guest)
echo  Health Check      : http://localhost:8080/actuator/health
echo  API Key           : unilabs-secret-key-2026
echo ===================================================
echo.
pause
