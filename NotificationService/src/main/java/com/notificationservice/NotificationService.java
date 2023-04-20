package com.notificationservice;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {
    private Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    private JavaMailSender mailSender;
    void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify Your Email Address");
        message.setText("Please click the following link to verify your email address: http://localhost:8080/api/user/verify?code=" + verificationCode);
        mailSender.send(message);

        logger.info("Verification email sent to {}", to);
    }
}
