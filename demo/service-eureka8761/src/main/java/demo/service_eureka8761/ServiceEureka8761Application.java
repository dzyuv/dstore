package demo.service_eureka8761;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceEureka8761Application {

    public static void main(String[] args) {
        SpringApplication.run(ServiceEureka8761Application.class, args);
    }

}
