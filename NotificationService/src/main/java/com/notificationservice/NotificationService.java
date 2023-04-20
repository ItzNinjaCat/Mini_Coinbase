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

    void sendLoginNotification(String to) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Login Notification");
        message.setText("You have logged in to your account.");
        mailSender.send(message);

        logger.info("Login notification sent to {}", to);
    }

    void sendLogoutNotification(String to) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Logout Notification");
        message.setText("You have logged out of your account.");
        mailSender.send(message);

        logger.info("Logout notification sent to {}", to);
    }

    void sendSuccessfullyVerifiedEmail(String to) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Successfully Verified");
        message.setText("You have successfully verified your email address.");
        mailSender.send(message);

        logger.info("Successfully verified email sent to {}", to);
    }

    void sendSuccessfullyDepositedEmail(String to, String amount, String currency) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Successfully Deposited");
        message.setText("You have successfully deposited " + amount + currency + " to your account.");
        mailSender.send(message);

        logger.info("Successfully deposited email sent to {}", to);
    }

    void sendSuccessfullyWithdrawnEmail(String to, String amount, String currency) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Successfully Withdrawn");
        message.setText("You have successfully withdrawn " + amount + currency + " from your account.");
        mailSender.send(message);

        logger.info("Successfully withdrawn email sent to {}", to);
    }

    void sendSuccessfullyExchanged(String to, String amount, String currency) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Successfully Exchanged");
        message.setText("You have successfully exchanged " + amount + currency + " from your account.");
        mailSender.send(message);

        logger.info("Successfully exchanged email sent to {}", to);
    }

    public void sendDepositFailedEmail(String email, String amount, String currency) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Deposit Failed");
        message.setText("You have failed to deposit " + amount + currency + " to your account.");
        mailSender.send(message);

        logger.info("Deposit failed email sent to {}", email);
    }

    public void sendWithdrawFailedEmail(String email, String amount, String currency) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Withdraw Failed");
        message.setText("You have failed to withdraw " + amount + currency + " from your account.");
        mailSender.send(message);

        logger.info("Withdraw failed email sent to {}", email);
    }

    public void sendTransferFailedEmail(String email, String amount, String currency) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Exchange Failed");
        message.setText("You have failed to exchange " + amount + currency + " from your account.");
        mailSender.send(message);

        logger.info("Exchange failed email sent to {}", email);
    }
}
