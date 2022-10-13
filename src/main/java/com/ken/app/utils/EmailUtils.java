package com.ken.app.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    private static final String KEN_TEST_EMAIL = "ken_test@gmail.com";

    public void sendSimpleMessage(String to, String subject, String text, List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(KEN_TEST_EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        if(list != null && list.size() > 0){
            message.setCc(getCcArray(list));
        }
        javaMailSender.send(message);
    }

    private String[] getCcArray(List<String> ccList){
        String[] cc = new String[ccList.size()];
        for(int i = 0; i<ccList.size(); i++){
            cc[i] = ccList.get(i);
        }
        return cc;
    }

    public void forgotMail(String to, String subject, String password) throws MessagingException{
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(KEN_TEST_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg ="<p><b>Your Login Details for Cafe Management System</b><br><b>Email: </b>" + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMsg,"text/html");
        javaMailSender.send(message);
    }
}
