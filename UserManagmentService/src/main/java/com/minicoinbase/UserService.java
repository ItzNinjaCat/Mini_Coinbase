package com.minicoinbase;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;


    @Value("${auth.address}")
    private String authAddress;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private RestTemplate restTemplate;
    public Cookie registerUser(SignUpDto signUpDto){
        User user = new User();
        user.setFirstName(signUpDto.getFirstName());
        user.setLastName(signUpDto.getLastName());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(signUpDto.getPassword());
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setEventType("userRegistered");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", user.getUsername());
        eventData.put("email", user.getEmail());
        eventData.put("created_at", user.getCreatedAt());
        eventData.put("token", token);
        event.setEventData(eventData);
        kafkaTemplate.send("user-events", "register", event);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Set the request headers
        return getCookie(user);
    }

    private Cookie getCookie(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(authAddress + "/auth/login?userId={user}",
                        HttpMethod.POST, requestEntity, String.class, user.getId());
        return new Cookie("jwt", responseEntity.getBody());
    }

    public Cookie login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElse(null);
        if(user == null){
            return null;
        }
        if(!passwordEncoder.matches(password, user.getPassword())){
            return null;
        }
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setEventType("userLoggedIn");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", user.getUsername());
        eventData.put("email", user.getEmail());
        event.setEventData(eventData);
        kafkaTemplate.send("user-events", "login", event);
        return getCookie(user);
    }

    public Cookie validateToken(String jwt_token, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt_token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity =
                restTemplate.exchange( authAddress + "/auth/validate?userId={user}",
                        HttpMethod.GET, requestEntity, String.class, userId);
        return new Cookie("jwt", responseEntity.getBody());
    }

    public void logout(String jwt_token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt_token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(authAddress + "/auth/logout",
                HttpMethod.POST, requestEntity, String.class);
    }

    public boolean verifyUser(String code) {
        User user = userRepository.findByVerificationToken(code).orElse(null);
        if(user == null){
            return false;
        }
        user.setVerificationToken(null);
        user.setVerified(true);
        userRepository.save(user);
        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setEventType("userVerified");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", user.getUsername());
        eventData.put("email", user.getEmail());
        event.setEventData(eventData);
        kafkaTemplate.send("user-events", "verified",event);
        return true;
    }
}
