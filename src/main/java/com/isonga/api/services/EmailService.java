
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
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    // Matches the key we set in application.properties
    @Value("${app.sendgrid.mail.from}")
    private String fromEmailAddress;

    /**
     * Sends the credential email asynchronously using SendGrid.
     */
    @Async
    public void sendCredentialEmail(String toEmail, String name, String password) {
        String subject = "Welcome to Isonga - Your Account Details";

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

        sendEmail(toEmail, subject, htmlContentString);
    }

    /**
     * Sends the Savings Summary email with details on Loan, Penalties, and Totals.
     */
    @Async
    public void sendSavingsSummary(String toEmail, String name, BigDecimal todaySaving, BigDecimal activeLoan, 
                                   BigDecimal availablePenalties, BigDecimal paidPenalties, 
                                   BigDecimal unpaidPenalties, BigDecimal totalSavings) {
        
        String subject = "Isonga Savings - Transaction Summary";

        String htmlContentString = """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    
                    <div style="background-color: #2ecc71; padding: 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Savings Confirmation</h1>
                    </div>

                    <div style="padding: 30px; color: #333333;">
                        <h2 style="color: #2c3e50; margin-top: 0;">Hello %s,</h2>
                        
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            We have received your savings contribution. Here is your current account summary:
                        </p>

                        <table style="width: 100%%; border-collapse: collapse; margin: 20px 0; font-size: 15px;">
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Today's Saving</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #2ecc71;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Active Loan</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #e74c3c;">%s</td>
                            </tr>
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Available Penalties</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Paid Penalties</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #2ecc71;">%s</td>
                            </tr>
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Unpaid Penalties</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #e74c3c;">%s</td>
                            </tr>
                            <tr style="background-color: #2c3e50; color: #fff;">
                                <td style="padding: 15px; font-weight: bold;">TOTAL SAVINGS</td>
                                <td style="padding: 15px; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                        </table>
                        
                        <div style="text-align: center; margin-top: 30px;">
                             <p style="font-size: 12px; color: #999;">Thank you for saving with Isonga.</p>
                        </div>
                    </div>
                    
                    <div style="background-color: #ecf0f1; padding: 20px; text-align: center; font-size: 12px; color: #7f8c8d;">
                        <p style="margin: 0;">&copy; 2026 Isonga Youth Savings. All rights reserved.</p>
                    </div>
                </div>
            </div>
            """.formatted(
                name, 
                formatMoney(todaySaving), 
                formatMoney(activeLoan),
                formatMoney(availablePenalties),
                formatMoney(paidPenalties),
                formatMoney(unpaidPenalties),
                formatMoney(totalSavings)
            );

        sendEmail(toEmail, subject, htmlContentString);
    }

    /**
     * Helper method to send emails via SendGrid to avoid code duplication.
     */
    private void sendEmail(String toEmail, String subject, String htmlContentString) {
        Email from = new Email(fromEmailAddress);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContentString);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email successfully sent to {} via SendGrid. Subject: '{}'", toEmail, subject);
            } else {
                log.error("Failed to send email to {}. Status: {}, Body: {}", toEmail, response.getStatusCode(), response.getBody());
            }

        } catch (IOException ex) {
            log.error("Network error sending email to {}: {}", toEmail, ex.getMessage());
        }
    }

    /**
     * Helper method to format BigDecimal amounts to 2 decimal places.
     */
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}