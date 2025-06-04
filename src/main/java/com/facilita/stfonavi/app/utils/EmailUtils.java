package com.facilita.stfonavi.app.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text, List<String> list) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("edwinrhc@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // ✅ Verificamos CC antes de setearlo
        if (list != null && !list.isEmpty()) {
            String[] cc = getCCArray(list);
            if (cc.length > 0) {
                message.setCc(cc);
            }
        }

        emailSender.send(message);
    }

    private String[] getCCArray(List<String> ccList) {

        return ccList.stream()
                .filter(email -> email != null && !email.isEmpty()) // ⚠️ evita nulls o vacíos or isBlanck
                .toArray(String[]::new);
    }

    public void forgotMail(String to, String subject, String password) throws MessagingException, MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("edwinrhc@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><b>Your Login details for Clinic App</b><br><b>Email: </b> " + to + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMsg, "text/html");
        emailSender.send(message);

    }

    public void sendHtmlMessage(String to, String subject, String htmlBody, List<String> ccList) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("edwinrhc@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true para HTML

        if (ccList != null && !ccList.isEmpty()) {
            String[] cc = getCCArray(ccList);
            if (cc.length > 0) {
                helper.setCc(cc);
            }
        }

        emailSender.send(message);
    }
}

