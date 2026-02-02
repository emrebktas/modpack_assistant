package com.example.chatbotmc.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {
    
    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from-email:noreply@yourdomain.com}")
    private String fromEmail;
    
    @Value("${sendgrid.from-name:Modpack Assistant}")
    private String fromName;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    public void sendAdminApprovalEmail(String username, String email, String approvalToken) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(adminEmail);
            String subject = "New user registration: " + username;
            
            String approveUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=approve";
            String rejectUrl = backendUrl + "/api/auth/approve-user?token=" + approvalToken + "&action=reject";
            
            String plainContent = buildAdminApprovalEmailPlain(username, email, approveUrl, rejectUrl);
            String htmlContent = buildAdminApprovalEmail(username, email, approveUrl, rejectUrl);
            
            Mail mail = new Mail(from, subject, to, new Content("text/plain", plainContent));
            mail.addContent(new Content("text/html", htmlContent));
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Email could not be sent: " + e.getMessage(), e);
        }
    }
    
    public void sendUserApprovalNotification(String userEmail, String username, boolean approved) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(userEmail);
            
            String subject;
            String plainContent;
            String htmlContent;
            
            if (approved) {
                subject = "Account approved - " + username;
                plainContent = buildUserApprovedEmailPlain(username);
                htmlContent = buildUserApprovedEmail(username);
            } else {
                subject = "Registration update - " + username;
                plainContent = buildUserRejectedEmailPlain(username);
                htmlContent = buildUserRejectedEmail(username);
            }
            
            Mail mail = new Mail(from, subject, to, new Content("text/plain", plainContent));
            mail.addContent(new Content("text/html", htmlContent));
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Email could not be sent: " + e.getMessage(), e);
        }
    }
    
    private String buildAdminApprovalEmailPlain(String username, String email, String approveUrl, String rejectUrl) {
        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return """
            New user registration

            A user has registered and needs your approval.

            Username: %s
            Email: %s
            Date: %s

            To approve: %s
            To reject: %s

            This is an automated message from the application.
            """.formatted(username, email, date, approveUrl, rejectUrl);
    }

    private String buildAdminApprovalEmail(String username, String email, String approveUrl, String rejectUrl) {
        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head>
            <body style="font-family: Arial, sans-serif; font-size: 14px; line-height: 1.5; color: #333; margin: 0; padding: 20px;">
                <p>New user registration</p>
                <p>A user has registered and needs your approval.</p>
                <p><strong>Username:</strong> %s<br />
                <strong>Email:</strong> %s<br />
                <strong>Date:</strong> %s</p>
                <p><a href="%s" style="color: #1967d2;">Approve</a> &nbsp;|&nbsp; <a href="%s" style="color: #1967d2;">Reject</a></p>
                <p style="font-size: 12px; color: #666;">This is an automated message from the application.</p>
            </body>
            </html>
            """.formatted(username, email, date, approveUrl, rejectUrl);
    }
    
    private String buildUserApprovedEmailPlain(String username) {
        return """
            Hello %s,

            Your account has been approved. You can log in and use the application.

            This is an automated message.
            """.formatted(username);
    }

    private String buildUserApprovedEmail(String username) {
        return """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head>
            <body style="font-family: Arial, sans-serif; font-size: 14px; line-height: 1.5; color: #333; margin: 0; padding: 20px;">
                <p>Hello %s,</p>
                <p>Your account has been approved. You can log in and use the application.</p>
                <p style="font-size: 12px; color: #666;">This is an automated message.</p>
            </body>
            </html>
            """.formatted(username);
    }
    
    private String buildUserRejectedEmailPlain(String username) {
        return """
            Hello %s,

            Your registration request could not be approved at this time. If you believe this is an error, please contact support.

            This is an automated message.
            """.formatted(username);
    }

    private String buildUserRejectedEmail(String username) {
        return """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /></head>
            <body style="font-family: Arial, sans-serif; font-size: 14px; line-height: 1.5; color: #333; margin: 0; padding: 20px;">
                <p>Hello %s,</p>
                <p>Your registration request could not be approved at this time. If you believe this is an error, please contact support.</p>
                <p style="font-size: 12px; color: #666;">This is an automated message.</p>
            </body>
            </html>
            """.formatted(username);
    }
}
