package com.notificationservice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${usr_mngmt}")
    private String usr_mngmt;
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

    @KafkaListener(topics = "deposit-completed", groupId = "deposit")
    public void listenGroupDeposit(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Long id = (Long) map.get("userId");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String email = restTemplate.exchange(usr_mngmt + "/getEmail?user={userId}", HttpMethod.GET, requestEntity , String.class, id).getBody();
        notificationService.sendSuccessfullyDepositedEmail(email, (String) map.get("amount"), (String) map.get("currency"));
    }

    @KafkaListener(topics = "withdraw-success", groupId = "withdraw")
    public void listenGroupWithdraw(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Long id = (Long) map.get("userId");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String email = restTemplate.exchange(usr_mngmt + "/getEmail?user={userId}", HttpMethod.GET, requestEntity , String.class, id).getBody();
        notificationService.sendSuccessfullyWithdrawnEmail(email, (String) map.get("amount"), (String) map.get("currency"));
    }

    @KafkaListener(topics = "transfer-success", groupId = "transfer")
    public void listenGroupTransfer(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Long id = (Long) map.get("userId");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String email = restTemplate.exchange(usr_mngmt + "/getEmail?user={userId}", HttpMethod.GET, requestEntity , String.class, id).getBody();
        if(map.get("status").equals("confirmed")){
            notificationService.sendSuccessfullyExchanged(email, (String) map.get("price"), (String) map.get("fiat_currency"), (String) map.get("quantity"),(String) map.get("crypto_currency"));

        }
        else{
            notificationService.sendTransferFailedEmail(email, (String) map.get("price"), (String) map.get("fiat_currency"), (String) map.get("quantity"),(String) map.get("crypto_currency"));
        }
    }

    @KafkaListener(topics = "user-verified", groupId = "userVerified")
    public void listenGroupUserVerified(String message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(message, Map.class);
        Map<String, String> eventData = (Map<String, String>) map.get("eventData");
        System.out.println("User verified: " + eventData.get("email"));
        notificationService.sendSuccessfullyVerifiedEmail(eventData.get("email"));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}