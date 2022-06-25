package com.kpjunaid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final Environment environment;

    @Override @Async
    public void send(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");
            messageHelper.setText(content, true);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email");
        }
    }

    @Override
    public String buildEmailVerifyMail(String token) {
        String url = environment.getProperty("app.root.frontend") + "/verify-email/" + token;
        return buildEmailBody(
                url,
                "Verify Email Address",
                "Please, click on the link below to verify your email address.",
                "Click to Verify"
        );
    }

    @Override
    public String buildResetPasswordMail(String token) {
        String url = environment.getProperty("app.root.frontend") + "/reset-password/" + token;
        return buildEmailBody(
                url,
                "Reset Your Password",
                "Please, click on the link below to get a new password.",
                "Get New Password"
        );
    }

    private String buildEmailBody(String url, String emailBodyHeader, String emailBodyDetail, String buttonText) {
        return "<div style=\"margin: 0 auto; width: 500px; text-align: center; background: #ffffff; border-radius: 5px; border: 3px solid #838383;\">" +
                    "<h2 style=\"background: #838383; padding: 15px; margin: 0; font-weight: 700; font-size: 24px; color: #ffffff;\">" + emailBodyHeader + "</h2>" +
                    "<p style=\"padding: 20px; font-size: 20px; color: #202020;\">" + emailBodyDetail + "</p>" +
                    "<a style=\"display: inline-block; padding: 10px 20px; margin-bottom: 30px; text-decoration: none; background: #3f51b5; font-size: 16px; border-radius: 3px; color: #ffffff;\" href=\" " + url + " \">" + buttonText + "</a>" +
                "</div>";
    }
}
