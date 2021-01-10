package com.ewallet.WalletService.Service;

import com.ewallet.WalletService.Util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class EmailService {
    public static final String FROM_EMAIL = "dreambigger10614@gmail.com";
    public static final String PASSWORD = "biggerdream100";
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public static void sendEmail(String toEmail) {
        Properties props = getProperties();
        Authenticator auth = getAuthenticator();
        Session session = Session.getDefaultInstance(props, auth);
        logger.info("Session created");
        EmailUtil.sendEmail(session, toEmail, "Mail from e_wallet", "Transaction");
    }

    public static void sendEmailWithAttachments(String host, String port,
                                                final String userName, final String password,
                                                String toAddress, String subject,
                                                String message, String attachFiles) {
        if(Objects.isNull(subject)) {
            subject = getDefaultSubject();
        }
        if(Objects.isNull(message)) {
            message = getDefaultMessage();
        }

        try {
            Properties properties = getProperties();
            Authenticator auth = getAuthenticator();
            Session session = Session.getInstance(properties,auth);

            Message msg = getEmailMessage(userName, toAddress, subject, session);
            Multipart multipart = getMultipartEmailContent(message, attachFiles);
            msg.setContent(multipart);

            sendSmtpEmail(session, msg);
        }
        catch (Exception e) {
            logger.info("Email not sent successfully"+e);
        }
    }

    private static void sendSmtpEmail(Session session, Message msg) throws MessagingException {
        //sends the email:
        Transport transport = session.getTransport("smtp");
        transport.connect("smtp.gmail.com",FROM_EMAIL,PASSWORD);
        transport.sendMessage(msg,msg.getAllRecipients());
        transport.close();
    }

    private static Multipart getMultipartEmailContent(String message, String attachFiles) throws MessagingException {
        MimeBodyPart messageBodyPart = getMimeMessageBodyPart(message);
        MimeBodyPart attachPart = getMimeBodyPartWithAttachment(attachFiles);
        //creates multi-part:
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachPart);
        return multipart;
    }

    private static MimeBodyPart getMimeBodyPartWithAttachment(String attachFiles) throws MessagingException {
        MimeBodyPart attachPart = new MimeBodyPart();

        try {
            attachPart.attachFile(attachFiles);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return attachPart;
    }

    private static MimeBodyPart getMimeMessageBodyPart(String message) throws MessagingException {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");
        return messageBodyPart;
    }

    private static Message getEmailMessage(String userName, String toAddress, String subject, Session session) throws MessagingException {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        return msg;
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return properties;
    }

    private static Authenticator getAuthenticator() {
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        };
        return auth;
    }
    private static String getDefaultSubject()
    {
        String subject = "Mail from e_wallet";
        return subject;
    }

    private static String getDefaultMessage()
    {
        String message = "Please find the attached file of your transaction history!!";
        return message;
    }

}
