package com.remote.targetExtraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.remote"})
@EnableDiscoveryClient
@SpringBootApplication
public class targetExtraction {
    public static void main(String[] args)
    {
        SpringApplication.run(targetExtraction.class,args);
    }
}