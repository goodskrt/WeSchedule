package com.iusjc.weschedule.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envoie un email de réinitialisation de mot de passe avec un lien d'accès direct
     */
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("IUSJC - Réinitialisation de votre mot de passe");
            
            String emailContent = buildPasswordResetEmailContent(recipientName, resetToken);
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email générique
     */
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email envoyé à: {}", toEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Construit le contenu du mail de réinitialisation avec lien direct
     */
    private String buildPasswordResetEmailContent(String recipientName, String resetToken) {
        String resetLink = "http://localhost:8080/reset-password?token=" + resetToken;
        return "Bonjour " + recipientName + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe pour IUSJC WeSchedule.\n\n" +
                "Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :\n" +
                resetLink + "\n\n" +
                "Ce lien est valable pendant 15 minutes.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe IUSJC\n\n" +
                "---\n" +
                "Message automatique - Veuillez ne pas répondre à cet email.";
    }
}
