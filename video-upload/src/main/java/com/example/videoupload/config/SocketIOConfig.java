package com.example.videoupload.config;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import jakarta.annotation.PreDestroy;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import io.github.cdimascio.dotenv.Dotenv;

@Component
public class SocketIOConfig {
	
    private SocketIOServer server;
	
    Dotenv dotenv = Dotenv.configure().load();

    String hostname = dotenv.get("SOCKET_IO_HOSTNAME");
	
    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(hostname);
        config.setPort(8082);
        server = new SocketIOServer(config);
        server.start();
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                System.out.println("new user connected with socket " + client.getSessionId());
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                client.getNamespace().getAllClients().stream().forEach(data -> {
                    System.out.println("user disconnected "+ data.getSessionId().toString());
                });
            }
        });

        return server;
    }

    @PreDestroy
    public void stopSocketIOServer() {
		this.server.stop();
	}
    
}
