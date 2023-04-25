package com.example.fiatservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class FiatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiatServiceApplication.class, args);
    }

    @Bean
    public NewTopic depositCompletedTopic() {
        return TopicBuilder.name("deposit-completed")
                .partitions(3)
//                .compact()
                .build();
    }

    @Bean
    public NewTopic withdrawCompletedTopic() {
        return TopicBuilder.name("withdraw-completed")
                .partitions(3)
//                .compact()
                .build();
    }

}
