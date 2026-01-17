package com.isonga.api.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${app.sendgrid.mail.from}")
    private String fromEmailAddress;

    /**
     * Sends the credential email asynchronously using SendGrid.
     */
    @Async
    public void sendCredentialEmail(String toEmail, String name, String password) {
        // 1. Configure Sender and Recipient
        Email from = new Email(fromEmailAddress);
        Email to = new Email(toEmail);
        String subject = "Welcome to Isonga - Your Account Details";

        // 2. Prepare HTML Content
        // We reuse your existing professional HTML template
        String htmlContentString = """
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

        Content content = new Content("text/html", htmlContentString);

        // 3. Construct the Mail Object
        Mail mail = new Mail(from, subject, to, content);

        // 4. Initialize SendGrid and Create Request
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // 5. Send Request
            Response response = sg.api(request);

            // 6. Logging based on Status Code
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Credential email successfully sent to {} via SendGrid. Status: {}", toEmail, response.getStatusCode());
            } else {
                log.error("Failed to send email via SendGrid. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            }

        } catch (IOException ex) {
            log.error("Network error sending email to {}: {}", toEmail, ex.getMessage());
        }
    }
}