package com.gridweaver.config;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
public class VirtualThreadConfig {

    /** General-purpose virtual-thread executor used by the IoT simulator and Kafka listeners. */
    @Bean(name = "virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /** Makes Tomcat itself dispatch each HTTP/WebSocket request on a virtual thread. */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return (ProtocolHandler protocolHandler) ->
                protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
