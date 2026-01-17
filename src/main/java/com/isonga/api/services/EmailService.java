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

    @Value("${app.sendgrid.mail.from}")
    private String fromEmailAddress;

    /**
     * Sends the credential email asynchronously.
     */
    @Async
    public void sendCredentialEmail(String toEmail, String name, String password) {
        String subject = "Murakaza neza Mu Isonga - Amakuru Ya Konti yanyu";

        String htmlContentString = """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background-color: #2ecc71; padding: 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Isonga Youth Savings</h1>
                    </div>
                    <div style="padding: 30px; color: #333333;">
                        <h2 style="color: #2c3e50; margin-top: 0;">Muraho %s!</h2>
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            Welcome to <b>Isonga Youth Savings</b>. Konti Yanyu Yafunguwe neza.
                        </p>
                        <div style="background-color: #f8f9fa; border-left: 5px solid #2ecc71; padding: 20px; margin: 25px 0; border-radius: 4px;">
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Username:</p>
                            <p style="margin: 0 0 15px 0; font-size: 18px; font-weight: bold; color: #333;">%s</p>
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Ijambo Banga Ry'agateganyo:</p>
                            <p style="margin: 0; font-size: 18px; font-family: monospace; font-weight: bold; color: #e74c3c; letter-spacing: 1px;">%s</p>
                        </div>
                        <p style="font-size: 14px; color: #e67e22;">
                            <b>Security Notice:</b> Please log in and change this password immediately.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(name, toEmail, password);

        sendEmail(toEmail, subject, htmlContentString);
    }

    /**
     * Sends the Savings Summary email.
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
                        <h2 style="color: #2c3e50; margin-top: 0;">Muraho %s!</h2>
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            Ubwizigame bwawe Bwakiriwe Neza. Rea Incamake yubwizigame bwawe:
                        </p>
                        <table style="width: 100%%; border-collapse: collapse; margin: 20px 0; font-size: 15px;">
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Ubwizigame Bw'uyu munsi</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #2ecc71;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Inguzanyo Ufite</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #e74c3c;">%s</td>
                            </tr>
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Ibihano Ufite</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Ibihano Byishyuwe</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #2ecc71;">%s</td>
                            </tr>
                            <tr style="background-color: #f8f9fa;">
                                <td style="padding: 12px; border-bottom: 1px solid #eee; color: #777;">Ibihano Bitishyuwe</td>
                                <td style="padding: 12px; border-bottom: 1px solid #eee; font-weight: bold; text-align: right; color: #e74c3c;">%s</td>
                            </tr>
                            <tr style="background-color: #2c3e50; color: #fff;">
                                <td style="padding: 15px; font-weight: bold;">Ubwizigame Bwose</td>
                                <td style="padding: 15px; font-weight: bold; text-align: right;">%s</td>
                            </tr>
                        </table>
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
     * ✅ NEW METHOD: Penalty Notification (Issued vs Paid)
     */
    @Async
    public void sendPenaltyNotification(String toEmail, String name, BigDecimal amount, String reason, String status) {
        boolean isPaid = "PAID".equalsIgnoreCase(status);
        String subject = isPaid ? "Ibihano Byishyuwe" : "Isonga - Ibihano Bishya";
        String color = isPaid ? "#2ecc71" : "#e74c3c"; // Green for Paid, Red for Issued

        String htmlContentString = """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f4f4f4; padding: 20px;">
                <div style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <div style="background-color: %s; padding: 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 24px;">%s</h1>
                    </div>
                    <div style="padding: 30px; color: #333333;">
                        <h2 style="color: #2c3e50; margin-top: 0;">Hello %s,</h2>
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            This is a notification regarding a penalty on your account.
                        </p>
                        <div style="background-color: #f8f9fa; border-left: 5px solid %s; padding: 20px; margin: 25px 0; border-radius: 4px;">
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Reason:</p>
                            <p style="margin: 0 0 15px 0; font-size: 16px; font-weight: bold; color: #333;">%s</p>
                            
                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Amount:</p>
                            <p style="margin: 0 0 15px 0; font-size: 18px; font-weight: bold; color: #333;">%s</p>

                            <p style="margin: 5px 0; font-size: 14px; color: #777;">Status:</p>
                            <p style="margin: 0; font-size: 16px; font-weight: bold; color: %s;">%s</p>
                        </div>
                    </div>
                    <div style="background-color: #ecf0f1; padding: 20px; text-align: center; font-size: 12px; color: #7f8c8d;">
                        <p style="margin: 0;">&copy; 2026 Isonga Youth Savings. All rights reserved.</p>
                    </div>
                </div>
            </div>
            """.formatted(color, subject, name, color, reason, formatMoney(amount), color, status);

        sendEmail(toEmail, subject, htmlContentString);
    }

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

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}