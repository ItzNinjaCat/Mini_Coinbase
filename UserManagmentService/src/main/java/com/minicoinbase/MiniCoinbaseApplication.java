package com.minicoinbase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
public class MiniCoinbaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniCoinbaseApplication.class, args);
    }

}
