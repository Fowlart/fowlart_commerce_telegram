package com.fowlart.main.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Component
public class GmailSender {

    private final String gmailAccName;
    private final String gmailAccEmailPassword;

    public GmailSender(
            @Value("${app.bot.email.gmail.user}") String gmailAccName,
            @Value("${app.bot.email.gmail.password}") String gmailAccEmailPassword) {

        this.gmailAccName = gmailAccName;
        this.gmailAccEmailPassword = gmailAccEmailPassword;
    }

    public void sendOrderMessage(String text, File csv) throws MessagingException, IOException {

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        // prop.put("mail.debug", "true");
        // prop.put("mail.debug.auth", "true");
        prop.put("mail.smtp.ssl.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(gmailAccName, gmailAccEmailPassword);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(gmailAccName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(gmailAccName));
        message.setSubject("Замовлення!");

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(text, "text/html; charset=utf-8");

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.attachFile(csv);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        multipart.addBodyPart(attachmentBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}