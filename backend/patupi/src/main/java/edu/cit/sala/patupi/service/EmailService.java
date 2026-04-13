package edu.cit.sala.patupi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Patupi Barber Shop");
            helper.setTo(toEmail);
            helper.setSubject(otp + " is your Patupi verification code");

            String htmlContent = 
                "<div style='font-family: Helvetica, Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;'>" +
                "  <div style='text-align: center; padding-bottom: 20px;'>" +
                "    <h1 style='color: #d4af37; margin: 0;'>PATUPI</h1>" +
                "    <p style='color: #888; font-size: 12px; text-transform: uppercase; letter-spacing: 2px;'>Precision & Style</p>" +
                "  </div>" +
                "  <div style='background-color: #f9f9f9; padding: 30px; border-radius: 8px; text-align: center;'>" +
                "    <p style='font-size: 16px; color: #333;'>Hello,</p>" +
                "    <p style='font-size: 16px; color: #333;'>Use the code below to verify your login. It expires in <strong>5 minutes</strong>.</p>" +
                "    <div style='margin: 30px 0; font-size: 32px; font-weight: bold; letter-spacing: 10px; color: #d4af37; background: white; display: inline-block; padding: 10px 20px; border-radius: 4px; border: 1px solid #ddd;'>" +
                     otp + 
                "    </div>" +
                "    <p style='font-size: 13px; color: #999;'>If you didn't request this, you can safely ignore this email.</p>" +
                "  </div>" +
                "  <p style='text-align: center; font-size: 12px; color: #aaa; margin-top: 20px;'>&copy; 2026 Patupi Barber Shop. All rights reserved.</p>" +
                "</div>";

            helper.setText(htmlContent, true); 
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public void sendSimpleEmail(String toEmail, String subject, String body) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, "Patupi Support");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        
        String htmlContent = "<div style='font-family: Arial; padding: 20px; border: 1px solid #ddd;'>" +
                             "<h3>Patupi Notification</h3>" +
                             "<p>" + body + "</p>" +
                             "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    } catch (Exception e) {
        throw new RuntimeException("Notification failed: " + e.getMessage());
    }
}
}