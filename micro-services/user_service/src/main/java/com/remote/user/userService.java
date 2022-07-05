package com.remote.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.remote"})
@EnableDiscoveryClient
@SpringBootApplication
public class userService {
    public static void main(String[] args)
    {
        SpringApplication.run(userService.class,args);
    }
}
