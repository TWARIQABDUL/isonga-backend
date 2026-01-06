package com.isonga.api.services;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    /**
     * Sends the credential email asynchronously.
     * Note: Added 'name' parameter to personalize the greeting (e.g., "Hello Twari").
     */
    @Async
    public void sendCredentialEmail(String toEmail, String name, String password) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Professional HTML Template with Java Text Blocks
            String htmlMsg = """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    
                    <div style="background-color: #2ecc71; padding: 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Isonga Youth Savings</h1>
                    </div>

                    <div style="padding: 30px; color: #333333;">
                        <h2 style="color: #2c3e50; margin-top: 0;">Hello %s!</h2>
                        
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            Welcome to <b>Isonga Youth Savings</b>. Your account has been successfully created. 
                            You can now access your dashboard.
                        </p>

                        <div style="background-color: #f8f9fa; border-left: 5px solid #2ecc71; padding: 20px; margin: 25px 0; border-radius: 4px;">
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Username:</p>
                            <p style="margin: 0 0 15px 0; font-size: 18px; font-weight: bold; color: #333;">%s</p>
                            
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Temporary Password:</p>
                            <p style="margin: 0; font-size: 18px; font-family: monospace; font-weight: bold; color: #e74c3c; letter-spacing: 1px;">%s</p>
                        </div>

                        <p style="font-size: 14px; color: #e67e22;">
                            <b>Security Notice:</b> Please log in and change this password immediately.
                        </p>
                        
                        <div style="text-align: center; margin-top: 30px;">
                             <p style="font-size: 12px; color: #999;">If you did not request this account, please ignore this email.</p>
                        </div>
                    </div>

                    <div style="background-color: #ecf0f1; padding: 20px; text-align: center; font-size: 12px; color: #7f8c8d;">
                        <p style="margin: 0;">&copy; 2026 Isonga Youth Savings. All rights reserved.</p>
                    </div>
                </div>
            </div>
            """.formatted(name, toEmail, password);

            helper.setText(htmlMsg, true); // Set to 'true' to enable HTML
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Isonga - Your Account Details");
            helper.setFrom(mailUsername);

            mailSender.send(mimeMessage);
            log.info("Credential email successfully sent to {}", toEmail);

        } catch (Exception e) {
            // We log the error instead of throwing it, so the app doesn't crash
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}