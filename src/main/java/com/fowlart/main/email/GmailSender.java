package com.fowlart.main.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@Component
public class GmailSender {

    private String gmailAccName;
    private String gmailAccEmailPassword;

    public GmailSender(
            @Value("${app.bot.email.gmail.user}") String gmailAccName,
            @Value("${app.bot.email.gmail.password}")String gmailAccEmailPassword) {

        this.gmailAccName = gmailAccName;
        this.gmailAccEmailPassword = gmailAccEmailPassword;
    }

    public void sendExampleMessage() throws MessagingException {

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.put("mail.debug", "true");
        prop.put("mail.debug.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(gmailAccName, gmailAccEmailPassword);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("fowlart1988@gmail.com"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse("fowlart1988@gmail.com"));
        message.setSubject("Order Example");

        String msg = "This is my first email using JavaMailer";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();

        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}