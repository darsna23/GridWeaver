package com.gridweaver.config;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Java 21 Project Loom configuration.
 *
 * Instead of one platform thread per IoT socket connection (which caps out
 * around a few thousand threads before the OS/JVM struggle), every
 * connection/task is handled on a cheap virtual thread. This is what lets
 * the mock IoT ingestion layer simulate 10,000+ concurrent node connections
 * using a fraction of the RAM a traditional thread-pool-per-connection
 * model would need.
 */
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
