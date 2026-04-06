package edu.cit.sala.patupi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    
    @Autowired
    private JavaMailSender mailSender;


    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("patupi.barbershop@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your Patupi Verification Code");
        message.setText("Welcome to Patupi! Your one-time password is: " + otp + 
                        "\n\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }


}