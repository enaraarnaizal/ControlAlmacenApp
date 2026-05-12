package com.example.controlalmacen.controller;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    public static void enviarGmail(
            String emailRemitente,
            String passwordAplicacion,
            String emailDestino,
            String asunto,
            String cuerpo
    ) throws Exception {

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailRemitente,
                        passwordAplicacion.replace(" ", "")
                );
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailRemitente));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
        message.setSubject(asunto, "UTF-8");
        message.setText(cuerpo, "UTF-8");

        Transport.send(message);
    }
}
