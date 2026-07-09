// src/main/java/com/gis/GisDashboardApplication.java
package com.gis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GisDashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(GisDashboardApplication.class, args);
        System.out.println("🚀 GIS Dashboard Backend Started!");
        System.out.println("📊 API: http://localhost:8080/api");
        System.out.println("🗄️  H2 Console: http://localhost:8080/h2-console");
        System.out.println("🔌 WebSocket: ws://localhost:8080/ws");
        System.out.println("✅ Ready to accept connections!");
    }
}