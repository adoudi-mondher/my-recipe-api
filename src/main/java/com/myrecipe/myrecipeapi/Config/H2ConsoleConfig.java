package com.myrecipe.myrecipeapi.Config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

@Profile("local")
@Configuration
public class H2ConsoleConfig {

    // Console web H2 → http://localhost:8082
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        return Server.createWebServer("-webAllowOthers", "-webPort", "8082");
    }

    // Serveur TCP → IntelliJ via jdbc:h2:tcp://localhost:9092/mem:myrecipedb
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        return Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092");
    }
}