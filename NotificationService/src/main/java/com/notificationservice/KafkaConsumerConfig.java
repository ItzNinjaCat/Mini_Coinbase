package com.notificationservice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Autowired
    private NotificationService notificationService;
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @KafkaListener(topics = "user-registered", groupId = "userRegistered")
    public void listenGroupUserRegistered(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        notificationService.sendVerificationEmail(eventData.get("email"), eventData.get("token"));
    }

    @KafkaListener(topics= "user-logged-in", groupId = "userLoggedIn")
    public void listenGroupUserLoggedIn(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("User logged in: " + eventData.get("email"));
        notificationService.sendLoginNotification(eventData.get("email"));
    }

    @KafkaListener(topics = "user-logged-out", groupId = "userLoggedOut")
    public void listenGroupUserLoggedOut(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("User logged out: " + eventData.get("email"));
        notificationService.sendLogoutNotification(eventData.get("email"));
    }

    @KafkaListener(topics = "deposit-success", groupId = "deposit")
    public void listenGroupDeposit(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Deposit: " + eventData.get("email"));
        notificationService.sendSuccessfullyDepositedEmail(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @KafkaListener(topics = "withdraw-success", groupId = "withdraw")
    public void listenGroupWithdraw(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Withdraw: " + eventData.get("email"));
        notificationService.sendSuccessfullyWithdrawnEmail(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @KafkaListener(topics = "transfer-success", groupId = "transfer")
    public void listenGroupTransfer(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Transfer: " + eventData.get("email"));
        notificationService.sendSuccessfullyExchanged(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @KafkaListener(topics = "user-verified", groupId = "userVerified")
    public void listenGroupUserVerified(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("User verified: " + eventData.get("email"));
        notificationService.sendSuccessfullyVerifiedEmail(eventData.get("email"));
    }

    @KafkaListener(topics = "deposit-fail", groupId = "depositFail")
    public void listenGroupDepositFail(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Deposit fail: " + eventData.get("email"));
        notificationService.sendDepositFailedEmail(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @KafkaListener(topics = "withdraw-fail", groupId = "withdrawFail")
    public void listenGroupWithdrawFail(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Withdraw fail: " + eventData.get("email"));
        notificationService.sendWithdrawFailedEmail(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @KafkaListener(topics = "transfer-fail", groupId = "transferFail")
    public void listenGroupTransferFail(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("Transfer fail: " + eventData.get("email"));
        notificationService.sendTransferFailedEmail(eventData.get("email"), eventData.get("amount"), eventData.get("currency"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}