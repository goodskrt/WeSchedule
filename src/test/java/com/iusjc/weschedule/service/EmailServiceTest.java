package com.iusjc.weschedule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private final String fromEmail = "test@weschedule.com";
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "baseUrl", baseUrl);
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmailWithCorrectContent() {
        // Arrange
        String toEmail = "user@example.com";
        String recipientName = "John Doe";
        String resetToken = "valid-token";

        // Act
        emailService.sendPasswordResetEmail(toEmail, recipientName, resetToken);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(fromEmail, sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("IUSJC - Réinitialisation de votre mot de passe", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(recipientName));
        assertTrue(sentMessage.getText().contains(resetToken));
        assertTrue(sentMessage.getText().contains(baseUrl + "/reset-password?token=" + resetToken));
    }

    @Test
    void sendEmail_ShouldSendGenericEmail() {
        // Arrange
        String toEmail = "user@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Act
        emailService.sendEmail(toEmail, subject, body);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(fromEmail, sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

    @Test
    void sendEmail_ShouldThrowException_WhenMailSenderFails() {
        // Arrange
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            emailService.sendEmail("test@test.com", "subject", "body")
        );
    }
}
