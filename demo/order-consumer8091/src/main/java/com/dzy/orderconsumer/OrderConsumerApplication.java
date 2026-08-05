package com.dzy.orderconsumer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.dzy.orderconsumer", "com.dzy.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan("com.dzy.orderconsumer.mapper")
public class OrderConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApplication.class, args);
    }
}