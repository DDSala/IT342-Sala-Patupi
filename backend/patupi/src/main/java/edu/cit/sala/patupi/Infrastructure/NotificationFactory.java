package edu.cit.sala.patupi.Infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {

    @Autowired
    private EmailService emailService;

    public void sendNotification(String type, String to, String subject, String body) {
        if ("EMAIL".equalsIgnoreCase(type)) {
           
            emailService.sendSimpleEmail(to, subject, body);
        }
    }
}